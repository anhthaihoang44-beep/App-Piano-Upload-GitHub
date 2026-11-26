import SwiftUI
import UIKit

public struct RecordingReplayScreen: View {

    let recording: RecordingEntity
    let audioEngine: AudioEngineProtocol
    let onBack: () -> Void

    @State private var isPlaying: Bool = false
    @State private var currentPositionMs: Int64 = 0
    @State private var highlightedKeys: [Int: UIColor] = [:]
    @State private var clock = MonotonicClock()
    @State private var playedIndices: Set<Int> = []
    @State private var loopTask: Task<Void, Never>?

    private let startMidi = 48
    private let endMidi = 84

    public init(
        recording: RecordingEntity,
        audioEngine: AudioEngineProtocol,
        onBack: @escaping () -> Void
    ) {
        self.recording = recording
        self.audioEngine = audioEngine
        self.onBack = onBack
    }

    private var song: Song {
        let decodedEvents: [RecordedNoteEvent]
        if let data = recording.notesJson.data(using: .utf8),
           let parsed = try? JSONDecoder().decode([RecordedNoteEvent].self, from: data) {
            decodedEvents = parsed
        } else {
            decodedEvents = []
        }

        let notes = decodedEvents.map {
            MidiNoteEvent(midi: $0.midiNumber, startMs: $0.startMs, durationMs: $0.durationMs, velocity: $0.velocity, hand: .right)
        }

        return Song(
            id: "recording_\(recording.recordingId)",
            title: recording.title,
            composer: "My Recording",
            difficulty: "Custom",
            bpm: 120,
            timeSignature: "4/4",
            isPremium: false,
            tracks: [SongTrack(hand: .right, notes: notes)]
        )
    }

    public var body: some View {
        let replaySong = song
        let maxDuration = max(1000.0, Double(recording.durationMs))

        VStack(spacing: 0) {
            HStack {
                Button {
                    stopPlayback()
                    onBack()
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.title3.bold())
                        .foregroundColor(.white)
                        .padding(8)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(recording.title)
                        .font(.headline)
                        .foregroundColor(.white)
                    Text("Recording Replay")
                        .font(.caption2)
                        .foregroundColor(PianoFlowColors.brandSecondary)
                }

                Spacer()
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 4)

            FallingNotesView(
                song: replaySong,
                currentPositionMs: currentPositionMs,
                startMidi: startMidi,
                endMidi: endMidi,
                keyWidthPt: 50
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            PianoCanvasView(
                startMidi: startMidi,
                endMidi: endMidi,
                keyWidthPt: 50,
                notationType: .cde,
                highlightedKeys: highlightedKeys,
                onKeyDown: { audioEngine.playNote(midiNumber: $0) },
                onKeyUp: { audioEngine.stopNote(midiNumber: $0) }
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            VStack(spacing: 8) {
                Slider(
                    value: Binding(
                        get: { min(max(0, Double(currentPositionMs)), maxDuration) },
                        set: { newValue in
                            let pos = Int64(newValue)
                            clock.seekTo(pos)
                            currentPositionMs = pos
                            playedIndices = Set(
                                replaySong.allNotes.enumerated().compactMap { $0.element.startMs < pos ? $0.offset : nil }
                            )
                        }
                    ),
                    in: 0...maxDuration
                )
                .tint(PianoFlowColors.brandPrimary)

                Button {
                    if isPlaying {
                        clock.pause()
                        isPlaying = false
                    } else {
                        clock.resume()
                        isPlaying = true
                    }
                } label: {
                    Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                        .font(.title3)
                        .foregroundColor(.white)
                        .frame(width: 44, height: 44)
                        .background(PianoFlowColors.brandPrimary)
                        .clipShape(Circle())
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(PianoFlowColors.surfaceDark)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .padding(8)
        }
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
        .onAppear {
            startPlayback(song: replaySong)
        }
        .onDisappear {
            stopPlayback()
        }
    }

    private func startPlayback(song: Song) {
        clock.start(fromPositionMs: 0)
        isPlaying = true
        loopTask?.cancel()
        loopTask = Task { @MainActor in
            while !Task.isCancelled {
                if clock.isPlaying {
                    let pos = clock.currentPositionMs()
                    currentPositionMs = pos

                    for (idx, note) in song.allNotes.enumerated() {
                        if !playedIndices.contains(idx) && pos >= note.startMs {
                            playedIndices.insert(idx)
                            audioEngine.playNote(midiNumber: note.midi)
                        }
                    }

                    var keys: [Int: UIColor] = [:]
                    for note in song.allNotes where pos >= (note.startMs - 80) && pos <= (note.startMs + note.durationMs) {
                        keys[note.midi] = PianoFlowColors.uiBrandSecondary
                    }
                    highlightedKeys = keys

                    if pos >= recording.durationMs + 1000 {
                        clock.pause()
                        isPlaying = false
                    }
                }
                try? await Task.sleep(nanoseconds: 16_000_000)
            }
        }
    }

    private func stopPlayback() {
        loopTask?.cancel()
        loopTask = nil
        clock.reset()
        audioEngine.stopAllNotes()
    }
}
