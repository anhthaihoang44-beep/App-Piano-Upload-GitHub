import Foundation

public struct Lesson: Identifiable, Hashable, Sendable {
    public let id: String
    public let stageId: String
    public let stageTitleKey: String
    public let titleKey: String
    public let descriptionKey: String
    public let instructionKey: String
    public let xpReward: Int
    public let targetNotes: [Int]
    public let targetSongId: String?
    public let isPremium: Bool

    public init(
        id: String,
        stageId: String,
        stageTitleKey: String,
        titleKey: String,
        descriptionKey: String,
        instructionKey: String,
        xpReward: Int,
        targetNotes: [Int],
        targetSongId: String? = nil,
        isPremium: Bool = false
    ) {
        self.id = id
        self.stageId = stageId
        self.stageTitleKey = stageTitleKey
        self.titleKey = titleKey
        self.descriptionKey = descriptionKey
        self.instructionKey = instructionKey
        self.xpReward = xpReward
        self.targetNotes = targetNotes
        self.targetSongId = targetSongId
        self.isPremium = isPremium
    }
}

public enum LessonCurriculum {

    public static let allLessons: [Lesson] = [
        // Stage 1: Piano Basics
        Lesson(
            id: "lesson_1_middle_c",
            stageId: "stage_1",
            stageTitleKey: "lessons_stage_basics",
            titleKey: "lesson_1_title",
            descriptionKey: "lesson_1_desc",
            instructionKey: "lesson_1_inst",
            xpReward: 50,
            targetNotes: [60, 60, 60],
            isPremium: false
        ),
        Lesson(
            id: "lesson_2_c_d_e",
            stageId: "stage_1",
            stageTitleKey: "lessons_stage_basics",
            titleKey: "lesson_2_title",
            descriptionKey: "lesson_2_desc",
            instructionKey: "lesson_2_inst",
            xpReward: 60,
            targetNotes: [60, 62, 64],
            isPremium: false
        ),
        Lesson(
            id: "lesson_3_black_keys",
            stageId: "stage_1",
            stageTitleKey: "lessons_stage_basics",
            titleKey: "lesson_3_title",
            descriptionKey: "lesson_3_desc",
            instructionKey: "lesson_3_inst",
            xpReward: 75,
            targetNotes: [60, 61, 62, 63],
            isPremium: false
        ),

        // Stage 2: Right Hand Technique
        Lesson(
            id: "lesson_4_five_fingers",
            stageId: "stage_2",
            stageTitleKey: "lessons_stage_right_hand",
            titleKey: "lesson_4_title",
            descriptionKey: "lesson_4_desc",
            instructionKey: "lesson_4_inst",
            xpReward: 80,
            targetNotes: [60, 62, 64, 65, 67],
            isPremium: false
        ),
        Lesson(
            id: "lesson_5_ode_to_joy_theme",
            stageId: "stage_2",
            stageTitleKey: "lessons_stage_right_hand",
            titleKey: "lesson_5_title",
            descriptionKey: "lesson_5_desc",
            instructionKey: "lesson_5_inst",
            xpReward: 100,
            targetNotes: [64, 64, 65, 67, 67, 65, 64, 62],
            targetSongId: "ode_to_joy",
            isPremium: false
        ),

        // Stage 3: Left Hand & Bass
        Lesson(
            id: "lesson_6_bass_c",
            stageId: "stage_3",
            stageTitleKey: "lessons_stage_left_hand",
            titleKey: "lesson_6_title",
            descriptionKey: "lesson_6_desc",
            instructionKey: "lesson_6_inst",
            xpReward: 75,
            targetNotes: [48, 48, 48],
            isPremium: true
        ),
        Lesson(
            id: "lesson_7_bass_fifth",
            stageId: "stage_3",
            stageTitleKey: "lessons_stage_left_hand",
            titleKey: "lesson_7_title",
            descriptionKey: "lesson_7_desc",
            instructionKey: "lesson_7_inst",
            xpReward: 85,
            targetNotes: [48, 55, 48, 55],
            isPremium: true
        ),

        // Stage 4: Both Hands Coordination
        Lesson(
            id: "lesson_8_two_hand_harmony",
            stageId: "stage_4",
            stageTitleKey: "lessons_stage_both_hands",
            titleKey: "lesson_8_title",
            descriptionKey: "lesson_8_desc",
            instructionKey: "lesson_8_inst",
            xpReward: 110,
            targetNotes: [48, 60, 55, 67],
            isPremium: true
        ),

        // Stage 5: Essential Chords
        Lesson(
            id: "lesson_9_c_major_chord",
            stageId: "stage_5",
            stageTitleKey: "lessons_stage_chords",
            titleKey: "lesson_9_title",
            descriptionKey: "lesson_9_desc",
            instructionKey: "lesson_9_inst",
            xpReward: 120,
            targetNotes: [60, 64, 67],
            isPremium: true
        ),
        Lesson(
            id: "lesson_10_g_major_chord",
            stageId: "stage_5",
            stageTitleKey: "lessons_stage_chords",
            titleKey: "lesson_10_title",
            descriptionKey: "lesson_10_desc",
            instructionKey: "lesson_10_inst",
            xpReward: 125,
            targetNotes: [55, 59, 62],
            isPremium: true
        ),

        // Stage 6: Master Songs
        Lesson(
            id: "lesson_11_canon_in_d",
            stageId: "stage_6",
            stageTitleKey: "lessons_stage_songs",
            titleKey: "lesson_11_title",
            descriptionKey: "lesson_11_desc",
            instructionKey: "lesson_11_inst",
            xpReward: 150,
            targetNotes: [74, 73, 71, 69],
            targetSongId: "canon_in_d",
            isPremium: true
        ),
        Lesson(
            id: "lesson_12_fur_elise_mastery",
            stageId: "stage_6",
            stageTitleKey: "lessons_stage_songs",
            titleKey: "lesson_12_title",
            descriptionKey: "lesson_12_desc",
            instructionKey: "lesson_12_inst",
            xpReward: 200,
            targetNotes: [76, 75, 76, 75, 76, 71, 74, 72, 69],
            targetSongId: "fur_elise",
            isPremium: true
        )
    ]

    public static func getById(_ id: String) -> Lesson? {
        allLessons.first { $0.id == id }
    }

    public static func isLessonUnlocked(
        at index: Int,
        completedLessonIds: Set<String>,
        lessons: [Lesson] = allLessons
    ) -> Bool {
        guard index >= 0 && index < lessons.count else { return false }
        if index == 0 { return true }
        let prevLesson = lessons[index - 1]
        return completedLessonIds.contains(prevLesson.id)
    }
}
