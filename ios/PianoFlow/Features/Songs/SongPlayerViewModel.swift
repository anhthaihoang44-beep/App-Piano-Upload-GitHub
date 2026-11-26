import Combine
import Foundation
import UIKit

private final class PlaybackLoopHandle: @unchecked Sendable {
    var task: Task<Void, Never>?
    deinit {
        task?.cancel()
    }
}

@MainActor
public final class SongPlayerViewModel: ObservableObject {

    @Published public private(set) var song: Song
    @Published public private(set) var playMode: PlayMode = .watch
    @Published public private(set) var isPlaying: Bool = false
    @Published public private(set) var currentPositionMs: Int64 = 0
    @Published public private(set) var selectedHand: HandType = .both
    @Published public private(set) var playbackSpeed: Float = 1.0
    @Published public private(set) var isLooping: Bool = false

    @Published public private(set) var score: Int = 0
    @Published public private(set) var currentCombo: Int = 0
    @Published public private(set) var maxCombo: Int = 0
    @Published public private(set) var lastHitRating: HitRating?
    @Published public private(set) var highlightedKeys: [Int: UIColor] = [:]
    @Published public private(set) var scoreSummary: ScoreSummary?

    private let audioEngine: AudioEngineProtocol
    private let dataStore: PianoFlowDataStore
    private let clock = MonotonicClock()
    private let loopHandle = PlaybackLoopHandle()

    private var playedSongNoteIndices: Set<Int> = []
    private var scoredNoteIndices: Set<Int> = []
    private var perfectHits: Int = 0
    private var goodHits: Int = 0

    public init(
        songId: String,
        audioEngine: AudioEngineProtocol,
        dataStore: PianoFlowDataStore
    ) {
        self.song = BundledSongs.getById(songId) ?? BundledSongs.allSongs[0]
        self.audioEngine = audioEngine
        self.dataStore = dataStore
        startClockLoop()
    }

    public func stopLoop() {
        loopHandle.task?.cancel()
        loopHandle.task = nil
        clock.reset()
        audioEngine.stopAllNotes()
    }

    private func startClockLoop() {
        loopHandle.task?.cancel()
        loopHandle.task = Task { [weak self] in
            while !Task.isCancelled {
                guard let self else { return }
                if self.clock.isPlaying {
                    let pos = self.clock.currentPositionMs()
                    self.currentPositionMs = pos
                    self.updateTargetKeys(pos: pos)

                    switch self.playMode {
                    case .watch:
                        self.handleWatchModePlayback(pos: pos)
                    case .follow:
                        self.handleFollowModeCheck(pos: pos)
                    case .perform:
                        break
                    }

                    if pos >= self.song.durationMs + 1000 {
                        if self.isLooping {
                            self.seekTo(0)
                        } else {
                            self.pause()
                            self.finishSongPerformance()
                        }
                    }
                }
                try? await Task.sleep(nanoseconds: 16_000_000)
            }
        }
    }

    private func updateTargetKeys(pos: Int64) {
        var targets: [Int: UIColor] = [:]
        for note in filteredNotes {
            if pos >= (note.startMs - 100) && pos <= (note.startMs + note.durationMs) {
                targets[note.midi] = note.hand == .left ? PianoFlowColors.uiLeftHand : PianoFlowColors.uiRightHand
            }
        }
        highlightedKeys = targets
    }

    private func handleWatchModePlayback(pos: Int64) {
        for (index, note) in filteredNotes.enumerated() {
            if !playedSongNoteIndices.contains(index) && pos >= note.startMs {
                playedSongNoteIndices.insert(index)
                audioEngine.playNote(midiNumber: note.midi, velocity: note.velocity)
            }
        }
    }

    private func handleFollowModeCheck(pos: Int64) {
        if let nextNote = filteredNotes.first(where: { $0.startMs > pos - 150 && $0.startMs <= pos + 50 }),
           pos >= nextNote.startMs {
            clock.pause()
            isPlaying = false
        }
    }

    public func onUserKeyDown(_ midiNumber: Int) {
        audioEngine.playNote(midiNumber: midiNumber)

        let pos = currentPositionMs
        let notes = filteredNotes

        if playMode == .follow {
            if notes.contains(where: { abs($0.startMs - pos) <= 150 && $0.midi == midiNumber }) {
                clock.resume()
                isPlaying = true
            }
        } else if playMode == .perform {
            if let candidateIndex = notes.indices.first(where: { idx in
                let note = notes[idx]
                return note.midi == midiNumber && !scoredNoteIndices.contains(idx) && abs(note.startMs - pos) <= 220
            }) {
                scoredNoteIndices.insert(candidateIndex)
                let diff = abs(notes[candidateIndex].startMs - pos)
                if diff <= 80 {
                    perfectHits += 1
                    currentCombo += 1
                    score += 100 + (currentCombo * 5)
                    lastHitRating = .perfect
                } else {
                    goodHits += 1
                    currentCombo += 1
                    score += 70 + (currentCombo * 2)
                    lastHitRating = .good
                }
                if currentCombo > maxCombo {
                    maxCombo = currentCombo
                }
            }
        }
    }

    public func onUserKeyUp(_ midiNumber: Int) {
        audioEngine.stopNote(midiNumber: midiNumber)
    }

    public func togglePlayPause() {
        if isPlaying {
            pause()
        } else {
            play()
        }
    }

    public func play() {
        clock.resume()
        isPlaying = true
    }

    public func pause() {
        clock.pause()
        isPlaying = false
    }

    public func seekTo(_ positionMs: Int64) {
        clock.seekTo(positionMs)
        currentPositionMs = positionMs
        playedSongNoteIndices.removeAll()
        for (index, note) in filteredNotes.enumerated() where note.startMs < positionMs {
            playedSongNoteIndices.insert(index)
        }
    }

    public func setMode(_ mode: PlayMode) {
        playMode = mode
        resetScoring()
    }

    public func setSpeed(_ speed: Float) {
        playbackSpeed = speed
        clock.setSpeed(speed)
    }

    public func toggleLoop() {
        isLooping.toggle()
    }

    public func setSelectedHand(_ hand: HandType) {
        selectedHand = hand
        resetScoring()
    }

    public func dismissSummary() {
        scoreSummary = nil
    }

    private func resetScoring() {
        score = 0
        currentCombo = 0
        maxCombo = 0
        lastHitRating = nil
        scoreSummary = nil
        scoredNoteIndices.removeAll()
        perfectHits = 0
        goodHits = 0
    }

    private var filteredNotes: [MidiNoteEvent] {
        switch selectedHand {
        case .both:
            return song.allNotes
        case .right:
            return song.tracks.filter { $0.hand == .right }.flatMap { $0.notes }
        case .left:
            return song.tracks.filter { $0.hand == .left }.flatMap { $0.notes }
        }
    }

    private func finishSongPerformance() {
        let totalNotes = filteredNotes.count
        guard totalNotes > 0 else { return }

        let summary = ScoringEngine.evaluateSummary(
            score: score,
            maxCombo: maxCombo,
            perfectHits: perfectHits,
            goodHits: goodHits,
            totalNotes: totalNotes
        )
        scoreSummary = summary

        dataStore.insertOrUpdateSongScore(
            songId: song.id,
            highScore: summary.score,
            stars: summary.stars,
            accuracyPercent: summary.accuracyPercent,
            maxCombo: summary.maxCombo
        )
    }
}
