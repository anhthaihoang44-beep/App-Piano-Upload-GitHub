import AVFoundation
import Combine
import Foundation

private final class NotificationObserverToken: @unchecked Sendable {
    private let token: NSObjectProtocol

    init(token: NSObjectProtocol) {
        self.token = token
    }

    deinit {
        NotificationCenter.default.removeObserver(token)
    }
}

/// High-performance, low-latency AVAudioEngine-based polyphonic piano synthesizer.
/// Pre-renders acoustic piano PCM buffers across all 88 keys (A0..C8) with 6 harmonics,
/// hammer strike transient, and exponential decay, dispatched across a 32-voice player pool.
@MainActor
public final class AVAudioPianoEngine: AudioEngineProtocol {

    public static let maxStreams = 32
    public static let minMidi = 21  // A0
    public static let maxMidi = 108 // C8
    public static let sampleRate: Double = 44_100
    public static let durationSeconds: Double = 1.6

    private let isLoadedSubject = CurrentValueSubject<Bool, Never>(false)
    private let loadingProgressSubject = CurrentValueSubject<Float, Never>(0)

    public var isLoaded: Bool { isLoadedSubject.value }
    public var loadingProgress: Float { loadingProgressSubject.value }
    public var isLoadedPublisher: AnyPublisher<Bool, Never> { isLoadedSubject.eraseToAnyPublisher() }
    public var loadingProgressPublisher: AnyPublisher<Float, Never> { loadingProgressSubject.eraseToAnyPublisher() }

    private let engine = AVAudioEngine()
    private var voicePool: [AVAudioPlayerNode] = []
    private var nextVoiceIndex: Int = 0
    private var voiceAssignedMidi: [Int?] = []
    private var activeNoteVoices: [Int: [Int]] = [:]
    private var buffers: [Int: AVAudioPCMBuffer] = [:]

    private let audioFormat: AVAudioFormat
    private var sustainEnabled: Bool = false
    private var masterVolume: Float = 0.9
    private var isInitializedGraph: Bool = false
    private var nextStreamToken: Int = 1
    private var configChangeToken: NotificationObserverToken?

    public init() {
        self.audioFormat = AVAudioFormat(standardFormatWithSampleRate: Self.sampleRate, channels: 1)!
    }

    public func initialize() async {
        if isLoadedSubject.value { return }

        configureAudioSession()
        setupAudioGraphIfNeeded()

        let minNote = Self.minMidi
        let maxNote = Self.maxMidi
        let totalNotes = maxNote - minNote + 1
        let format = audioFormat

        let generated: [(Int, AVAudioPCMBuffer)] = await Task.detached(priority: .userInitiated) {
            var list: [(Int, AVAudioPCMBuffer)] = []
            list.reserveCapacity(totalNotes)
            for midi in minNote...maxNote {
                if let buffer = Self.synthesizeAcousticPianoBuffer(midiNumber: midi, format: format) {
                    list.append((midi, buffer))
                }
            }
            return list
        }.value

        for (index, item) in generated.enumerated() {
            buffers[item.0] = item.1
            if index % 8 == 0 || index == generated.count - 1 {
                loadingProgressSubject.send(Float(index + 1) / Float(max(1, generated.count)))
            }
        }

        startEngineIfNeeded()
        isLoadedSubject.send(true)
    }

    @discardableResult
    public func playNote(midiNumber: Int, velocity: Float = 1.0) -> Int {
        guard let buffer = buffers[midiNumber] ?? closestBuffer(for: midiNumber) else {
            return 0
        }

        startEngineIfNeeded()

        let voiceIndex = allocateVoiceIndex()
        let player = voicePool[voiceIndex]

        if let previousMidi = voiceAssignedMidi[voiceIndex] {
            activeNoteVoices[previousMidi]?.removeAll { $0 == voiceIndex }
        }

        let clampedVelocity = min(max(velocity, 0.1), 1.0)
        player.stop()
        player.volume = min(max(masterVolume * clampedVelocity, 0.0), 1.0)
        player.scheduleBuffer(buffer, at: nil, options: .interrupts)
        player.play()

        voiceAssignedMidi[voiceIndex] = midiNumber
        var voices = activeNoteVoices[midiNumber] ?? []
        voices.append(voiceIndex)
        if voices.count > 4 {
            voices.removeFirst()
        }
        activeNoteVoices[midiNumber] = voices

        nextStreamToken += 1
        return nextStreamToken
    }

    public func stopNote(midiNumber: Int) {
        guard !sustainEnabled else { return }
        guard let voices = activeNoteVoices[midiNumber], !voices.isEmpty else { return }

        for idx in voices {
            if idx >= 0 && idx < voicePool.count {
                voicePool[idx].stop()
                voiceAssignedMidi[idx] = nil
            }
        }
        activeNoteVoices[midiNumber] = nil
    }

    public func stopAllNotes() {
        for idx in voicePool.indices {
            voicePool[idx].stop()
            voiceAssignedMidi[idx] = nil
        }
        activeNoteVoices.removeAll()
    }

    public func setSustain(_ enabled: Bool) {
        sustainEnabled = enabled
        if !enabled {
            stopAllNotes()
        }
    }

    public func isSustainEnabled() -> Bool {
        sustainEnabled
    }

    public func setVolume(_ volume: Float) {
        masterVolume = min(max(volume, 0.0), 1.0)
        engine.mainMixerNode.outputVolume = masterVolume
        for player in voicePool where player.isPlaying {
            player.volume = masterVolume
        }
    }

    public func getVolume() -> Float {
        masterVolume
    }

    public func release() {
        stopAllNotes()
        engine.stop()
        buffers.removeAll()
        isLoadedSubject.send(false)
        loadingProgressSubject.send(0)
    }

    private func configureAudioSession() {
        #if os(iOS)
        let session = AVAudioSession.sharedInstance()
        try? session.setCategory(.playback, mode: .default, options: [.mixWithOthers])
        try? session.setPreferredIOBufferDuration(0.005)
        try? session.setActive(true)
        #endif
    }

    private func setupAudioGraphIfNeeded() {
        guard !isInitializedGraph else { return }
        isInitializedGraph = true

        voicePool = (0..<Self.maxStreams).map { _ in AVAudioPlayerNode() }
        voiceAssignedMidi = Array(repeating: nil, count: Self.maxStreams)

        for player in voicePool {
            engine.attach(player)
            engine.connect(player, to: engine.mainMixerNode, format: audioFormat)
        }

        engine.mainMixerNode.outputVolume = masterVolume
        engine.prepare()

        let token = NotificationCenter.default.addObserver(
            forName: .AVAudioEngineConfigurationChange,
            object: engine,
            queue: .main
        ) { [weak self] _ in
            Task { @MainActor in
                self?.startEngineIfNeeded()
            }
        }
        configChangeToken = NotificationObserverToken(token: token)
    }

    private func startEngineIfNeeded() {
        guard isInitializedGraph else { return }
        if !engine.isRunning {
            try? engine.start()
        }
    }

    private func allocateVoiceIndex() -> Int {
        for index in voicePool.indices where !voicePool[index].isPlaying {
            return index
        }
        let chosen = nextVoiceIndex
        nextVoiceIndex = (nextVoiceIndex + 1) % Self.maxStreams
        return chosen
    }

    private func closestBuffer(for midiNumber: Int) -> AVAudioPCMBuffer? {
        guard let closestKey = buffers.keys.min(by: { abs($0 - midiNumber) < abs($1 - midiNumber) }) else {
            return nil
        }
        return buffers[closestKey]
    }

    /// Synthesizes an acoustic grand piano waveform with fundamental + 6 harmonics,
    /// 8ms hammer strike click transient, and natural exponential decay.
    private nonisolated static func synthesizeAcousticPianoBuffer(
        midiNumber: Int,
        format: AVAudioFormat
    ) -> AVAudioPCMBuffer? {
        let frameCount = AVAudioFrameCount(sampleRate * durationSeconds)
        guard let buffer = AVAudioPCMBuffer(pcmFormat: format, frameCapacity: frameCount),
              let channelData = buffer.floatChannelData?[0] else {
            return nil
        }
        buffer.frameLength = frameCount

        let freq = NotePitch.fromMidi(midiNumber)?.frequencyHz ?? 440.0
        let decayRate = 2.2 + Float(max(0, midiNumber - 36)) * 0.045
        let sr = Float(sampleRate)

        var seed: UInt64 = UInt64(midiNumber) &* 6364136223846793005 &+ 1

        for i in 0..<Int(frameCount) {
            let t = Float(i) / sr
            let attackRamp = min(1.0, t / 0.002)
            let envelope = exp(-decayRate * t) * attackRamp

            var hammerNoise: Float = 0
            if t < 0.008 {
                seed = seed &* 2862933555777941757 &+ 3037000493
                let randUnit = Float(Int32(bitPattern: UInt32(seed >> 32))) / Float(Int32.max)
                hammerNoise = randUnit * 0.5 * exp(-t * 600.0) * 0.35
            }

            var sample: Float = 0.65 * sin(2.0 * Float.pi * freq * t)
            sample += 0.25 * sin(2.0 * Float.pi * 2.0 * freq * t)
            sample += 0.15 * sin(2.0 * Float.pi * 3.0 * freq * t)
            sample += 0.08 * sin(2.0 * Float.pi * 4.0 * freq * t)
            sample += 0.04 * sin(2.0 * Float.pi * 5.0 * freq * t)
            sample += 0.02 * sin(2.0 * Float.pi * 6.0 * freq * t)

            let combined = (sample + hammerNoise) * envelope * 0.5
            channelData[i] = min(max(combined, -1.0), 1.0)
        }

        return buffer
    }
}
