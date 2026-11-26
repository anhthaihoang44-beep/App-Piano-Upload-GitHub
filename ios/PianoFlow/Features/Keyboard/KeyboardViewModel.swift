import Combine
import Foundation

@MainActor
public final class KeyboardViewModel: ObservableObject {

    private let audioEngine: AudioEngineProtocol
    private let preferencesRepository: UserPreferencesRepository
    private let dataStore: PianoFlowDataStore
    private let hapticHelper: HapticHelper

    @Published public private(set) var isAudioLoaded: Bool = false
    @Published public private(set) var audioLoadingProgress: Float = 0
    @Published public private(set) var notationType: NotationType = .cde
    @Published public private(set) var isSustainEnabled: Bool = false
    @Published public private(set) var masterVolume: Float = 0.9
    @Published public private(set) var hapticsEnabled: Bool = true

    @Published public var scrollOffset: CGFloat = 0
    @Published public private(set) var keyWidthPt: CGFloat = 54
    @Published public private(set) var currentOctaveOffset: Int = 0

    @Published public private(set) var isRecording: Bool = false
    @Published public private(set) var recordingDurationMs: Int64 = 0

    private var recordingStartTimeMs: Int64 = 0
    private var recordedNotesList: [RecordedNoteEvent] = []
    private var activeHeldNotes: [Int: Int64] = [:]
    private var cancellables: Set<AnyCancellable> = []

    public init(
        audioEngine: AudioEngineProtocol,
        preferencesRepository: UserPreferencesRepository,
        dataStore: PianoFlowDataStore,
        hapticHelper: HapticHelper
    ) {
        self.audioEngine = audioEngine
        self.preferencesRepository = preferencesRepository
        self.dataStore = dataStore
        self.hapticHelper = hapticHelper

        self.isAudioLoaded = audioEngine.isLoaded
        self.audioLoadingProgress = audioEngine.loadingProgress
        self.notationType = preferencesRepository.notationType
        self.isSustainEnabled = preferencesRepository.sustainEnabled
        self.masterVolume = preferencesRepository.volume
        self.hapticsEnabled = preferencesRepository.hapticsEnabled

        audioEngine.isLoadedPublisher
            .receive(on: RunLoop.main)
            .assign(to: &$isAudioLoaded)

        audioEngine.loadingProgressPublisher
            .receive(on: RunLoop.main)
            .assign(to: &$audioLoadingProgress)

        preferencesRepository.$notationType
            .receive(on: RunLoop.main)
            .assign(to: &$notationType)

        preferencesRepository.$sustainEnabled
            .receive(on: RunLoop.main)
            .sink { [weak self] enabled in
                self?.isSustainEnabled = enabled
                self?.audioEngine.setSustain(enabled)
            }
            .store(in: &cancellables)

        preferencesRepository.$volume
            .receive(on: RunLoop.main)
            .sink { [weak self] vol in
                self?.masterVolume = vol
                self?.audioEngine.setVolume(vol)
            }
            .store(in: &cancellables)

        preferencesRepository.$hapticsEnabled
            .receive(on: RunLoop.main)
            .assign(to: &$hapticsEnabled)
    }

    public func onKeyDown(_ midiNumber: Int) {
        let nowMs = Int64(Date().timeIntervalSince1970 * 1000)
        audioEngine.playNote(midiNumber: midiNumber)
        if hapticsEnabled {
            hapticHelper.keyPress()
        }
        if isRecording {
            activeHeldNotes[midiNumber] = nowMs
        }
    }

    public func onKeyUp(_ midiNumber: Int) {
        audioEngine.stopNote(midiNumber: midiNumber)
        if isRecording, let startTimestamp = activeHeldNotes.removeValue(forKey: midiNumber) {
            let startMs = max(0, startTimestamp - recordingStartTimeMs)
            let nowMs = Int64(Date().timeIntervalSince1970 * 1000)
            let durationMs = max(100, nowMs - startTimestamp)
            recordedNotesList.append(
                RecordedNoteEvent(midiNumber: midiNumber, startMs: startMs, durationMs: durationMs)
            )
        }
    }

    public func toggleSustain() {
        let next = !isSustainEnabled
        preferencesRepository.setSustainEnabled(next)
        audioEngine.setSustain(next)
    }

    public func cycleNotationType() {
        let next: NotationType
        switch notationType {
        case .cde:
            next = .doReMi
        case .doReMi:
            next = .off
        case .off:
            next = .cde
        }
        preferencesRepository.setNotationType(next)
    }

    public func setVolume(_ value: Float) {
        preferencesRepository.setVolume(value)
        audioEngine.setVolume(value)
    }

    public func zoomIn() {
        keyWidthPt = min(keyWidthPt + 6, 80)
    }

    public func zoomOut() {
        keyWidthPt = max(keyWidthPt - 6, 36)
    }

    public func resetZoom() {
        keyWidthPt = 54
        scrollOffset = 0
    }

    public func setScrollOffset(_ offset: CGFloat) {
        scrollOffset = max(0, offset)
    }

    public func shiftOctave(_ delta: Int) {
        currentOctaveOffset = min(max(currentOctaveOffset + delta, -2), 2)
    }

    public func startRecording() {
        recordedNotesList.removeAll()
        activeHeldNotes.removeAll()
        recordingStartTimeMs = Int64(Date().timeIntervalSince1970 * 1000)
        isRecording = true
        recordingDurationMs = 0
    }

    public func stopRecording() -> (noteCount: Int, durationMs: Int64) {
        guard isRecording else { return (0, 0) }
        isRecording = false
        let nowMs = Int64(Date().timeIntervalSince1970 * 1000)
        let durationMs = max(0, nowMs - recordingStartTimeMs)

        for (midi, startTimestamp) in activeHeldNotes {
            let startMs = max(0, startTimestamp - recordingStartTimeMs)
            let noteDuration = max(120, nowMs - startTimestamp)
            recordedNotesList.append(
                RecordedNoteEvent(midiNumber: midi, startMs: startMs, durationMs: noteDuration)
            )
        }
        activeHeldNotes.removeAll()
        recordingDurationMs = durationMs
        return (recordedNotesList.count, durationMs)
    }

    public func saveRecording(title: String, durationMs: Int64) {
        let trimmed = title.trimmingCharacters(in: .whitespacesAndNewlines)
        let finalTitle = trimmed.isEmpty
            ? "Piano Melody \(Int64(Date().timeIntervalSince1970 * 1000) % 10000)"
            : trimmed
        let sorted = recordedNotesList.sorted { $0.startMs < $1.startMs }
        let jsonData = (try? JSONEncoder().encode(sorted)) ?? Data("[]".utf8)
        let jsonString = String(data: jsonData, encoding: .utf8) ?? "[]"

        dataStore.insertRecording(
            title: finalTitle,
            durationMs: durationMs,
            noteCount: sorted.count,
            notesJson: jsonString
        )
    }
}
