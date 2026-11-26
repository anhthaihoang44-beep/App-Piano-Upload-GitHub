import Foundation
import SwiftData

@Model
public final class LessonProgressEntity {
    @Attribute(.unique) public var lessonId: String
    public var stageId: String
    public var isCompleted: Bool
    public var stars: Int
    public var bestAccuracy: Float
    public var xpEarned: Int
    public var completedAtMs: Int64

    public init(
        lessonId: String,
        stageId: String,
        isCompleted: Bool = false,
        stars: Int = 0,
        bestAccuracy: Float = 0,
        xpEarned: Int = 0,
        completedAtMs: Int64 = 0
    ) {
        self.lessonId = lessonId
        self.stageId = stageId
        self.isCompleted = isCompleted
        self.stars = stars
        self.bestAccuracy = bestAccuracy
        self.xpEarned = xpEarned
        self.completedAtMs = completedAtMs
    }
}

@Model
public final class RecordingEntity {
    @Attribute(.unique) public var recordingId: String
    public var title: String
    public var createdAtMs: Int64
    public var durationMs: Int64
    public var noteCount: Int
    public var notesJson: String
    public var audioFilePath: String?

    public init(
        recordingId: String = UUID().uuidString,
        title: String,
        createdAtMs: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        durationMs: Int64,
        noteCount: Int,
        notesJson: String,
        audioFilePath: String? = nil
    ) {
        self.recordingId = recordingId
        self.title = title
        self.createdAtMs = createdAtMs
        self.durationMs = durationMs
        self.noteCount = noteCount
        self.notesJson = notesJson
        self.audioFilePath = audioFilePath
    }
}

@Model
public final class SongScoreEntity {
    @Attribute(.unique) public var songId: String
    public var highScore: Int
    public var stars: Int
    public var accuracyPercent: Float
    public var maxCombo: Int
    public var lastPlayedAtMs: Int64

    public init(
        songId: String,
        highScore: Int = 0,
        stars: Int = 0,
        accuracyPercent: Float = 0,
        maxCombo: Int = 0,
        lastPlayedAtMs: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.songId = songId
        self.highScore = highScore
        self.stars = stars
        self.accuracyPercent = accuracyPercent
        self.maxCombo = maxCombo
        self.lastPlayedAtMs = lastPlayedAtMs
    }
}
