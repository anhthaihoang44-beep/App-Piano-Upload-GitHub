import Combine
import Foundation
import SwiftData

/// Reactive SwiftData-backed local store matching Android Room DAOs.
@MainActor
public final class PianoFlowDataStore: ObservableObject {

    public let container: ModelContainer
    private let context: ModelContext

    @Published public private(set) var lessonProgress: [LessonProgressEntity] = []
    @Published public private(set) var recordings: [RecordingEntity] = []
    @Published public private(set) var songScores: [SongScoreEntity] = []

    public var totalXp: Int {
        lessonProgress.filter { $0.isCompleted }.reduce(0) { $0 + $1.xpEarned }
    }

    public var completedLessonsCount: Int {
        lessonProgress.filter { $0.isCompleted }.count
    }

    public var completedSongsCount: Int {
        songScores.filter { $0.stars > 0 }.count
    }

    public init(inMemory: Bool = false) {
        let schema = Schema([
            LessonProgressEntity.self,
            RecordingEntity.self,
            SongScoreEntity.self
        ])
        let configuration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: inMemory)
        do {
            let modelContainer = try ModelContainer(for: schema, configurations: [configuration])
            self.container = modelContainer
            self.context = modelContainer.mainContext
        } catch {
            let fallbackConfig = ModelConfiguration(schema: schema, isStoredInMemoryOnly: true)
            let fallbackContainer = try! ModelContainer(for: schema, configurations: [fallbackConfig])
            self.container = fallbackContainer
            self.context = fallbackContainer.mainContext
        }
        reloadAll()
    }

    public func reloadAll() {
        lessonProgress = (try? context.fetch(FetchDescriptor<LessonProgressEntity>())) ?? []
        let recordingDescriptor = FetchDescriptor<RecordingEntity>(
            sortBy: [SortDescriptor(\.createdAtMs, order: .reverse)]
        )
        recordings = (try? context.fetch(recordingDescriptor)) ?? []
        songScores = (try? context.fetch(FetchDescriptor<SongScoreEntity>())) ?? []
    }

    public func saveLessonProgress(
        lessonId: String,
        stageId: String,
        stars: Int,
        bestAccuracy: Float,
        xpEarned: Int
    ) {
        let nowMs = Int64(Date().timeIntervalSince1970 * 1000)
        if let existing = lessonProgress.first(where: { $0.lessonId == lessonId }) {
            existing.isCompleted = true
            existing.stars = max(existing.stars, stars)
            existing.bestAccuracy = max(existing.bestAccuracy, bestAccuracy)
            existing.xpEarned = max(existing.xpEarned, xpEarned)
            existing.completedAtMs = nowMs
        } else {
            let entity = LessonProgressEntity(
                lessonId: lessonId,
                stageId: stageId,
                isCompleted: true,
                stars: stars,
                bestAccuracy: bestAccuracy,
                xpEarned: xpEarned,
                completedAtMs: nowMs
            )
            context.insert(entity)
        }
        try? context.save()
        reloadAll()
    }

    public func insertRecording(
        title: String,
        durationMs: Int64,
        noteCount: Int,
        notesJson: String
    ) {
        let entity = RecordingEntity(
            title: title,
            durationMs: durationMs,
            noteCount: noteCount,
            notesJson: notesJson
        )
        context.insert(entity)
        try? context.save()
        reloadAll()
    }

    public func deleteRecording(_ recording: RecordingEntity) {
        context.delete(recording)
        try? context.save()
        reloadAll()
    }

    public func insertOrUpdateSongScore(
        songId: String,
        highScore: Int,
        stars: Int,
        accuracyPercent: Float,
        maxCombo: Int
    ) {
        let nowMs = Int64(Date().timeIntervalSince1970 * 1000)
        if let existing = songScores.first(where: { $0.songId == songId }) {
            existing.highScore = max(existing.highScore, highScore)
            existing.stars = max(existing.stars, stars)
            existing.accuracyPercent = max(existing.accuracyPercent, accuracyPercent)
            existing.maxCombo = max(existing.maxCombo, maxCombo)
            existing.lastPlayedAtMs = nowMs
        } else {
            let entity = SongScoreEntity(
                songId: songId,
                highScore: highScore,
                stars: stars,
                accuracyPercent: accuracyPercent,
                maxCombo: maxCombo,
                lastPlayedAtMs: nowMs
            )
            context.insert(entity)
        }
        try? context.save()
        reloadAll()
    }
}
