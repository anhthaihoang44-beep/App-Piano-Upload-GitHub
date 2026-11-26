import Foundation

public struct Lesson: Identifiable, Hashable, Sendable {
    public let id: String
    public let stageId: String
    public let stageTitleKey: String
    public let title: String
    public let description: String
    public let instruction: String
    public let xpReward: Int
    public let targetNotes: [Int]
    public let targetSongId: String?
    public let isPremium: Bool

    public init(
        id: String,
        stageId: String,
        stageTitleKey: String,
        title: String,
        description: String,
        instruction: String,
        xpReward: Int,
        targetNotes: [Int],
        targetSongId: String? = nil,
        isPremium: Bool = false
    ) {
        self.id = id
        self.stageId = stageId
        self.stageTitleKey = stageTitleKey
        self.title = title
        self.description = description
        self.instruction = instruction
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
            title: "Find Middle C",
            description: "The central home base of the piano keyboard.",
            instruction: "Middle C (C4) is located directly to the left of the group of two black keys near the center. Press Middle C to start!",
            xpReward: 50,
            targetNotes: [60, 60, 60],
            isPremium: false
        ),
        Lesson(
            id: "lesson_2_c_d_e",
            stageId: "stage_1",
            stageTitleKey: "lessons_stage_basics",
            title: "The First Trio: C, D, E",
            description: "Learn your first three consecutive white keys.",
            instruction: "Place your fingers on C4, D4, and E4. Play them in order from left to right: C -> D -> E.",
            xpReward: 60,
            targetNotes: [60, 62, 64],
            isPremium: false
        ),
        Lesson(
            id: "lesson_3_black_keys",
            stageId: "stage_1",
            stageTitleKey: "lessons_stage_basics",
            title: "Black Keys: C# and D#",
            description: "Discover sharps and the distinctive twin black keys.",
            instruction: "The two black keys above C and D are C# (sharp) and D#. Play C4, C#4, D4, D#4.",
            xpReward: 75,
            targetNotes: [60, 61, 62, 63],
            isPremium: false
        ),

        // Stage 2: Right Hand Technique
        Lesson(
            id: "lesson_4_five_fingers",
            stageId: "stage_2",
            stageTitleKey: "lessons_stage_right_hand",
            title: "5-Finger Pentascale (C–G)",
            description: "Use all five fingers on one position.",
            instruction: "Assign Thumb (1) to C, Index (2) to D, Middle (3) to E, Ring (4) to F, Pinky (5) to G. Play C -> D -> E -> F -> G.",
            xpReward: 80,
            targetNotes: [60, 62, 64, 65, 67],
            isPremium: false
        ),
        Lesson(
            id: "lesson_5_ode_to_joy_theme",
            stageId: "stage_2",
            stageTitleKey: "lessons_stage_right_hand",
            title: "Ode to Joy Theme",
            description: "Play Beethoven's beloved melody using the 5-finger position.",
            instruction: "Play the opening phrase: E -> E -> F -> G -> G -> F -> E -> D.",
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
            title: "Bass C & Low Register",
            description: "Anchor your left hand an octave lower.",
            instruction: "Look one octave to the left for Bass C (C3). Press C3 with your left hand pinky.",
            xpReward: 75,
            targetNotes: [48, 48, 48],
            isPremium: true
        ),
        Lesson(
            id: "lesson_7_bass_fifth",
            stageId: "stage_3",
            stageTitleKey: "lessons_stage_left_hand",
            title: "The Power of the 5th (C & G)",
            description: "The foundational accompaniment interval in modern music.",
            instruction: "Play Bass C (C3) followed by Bass G (G3): low and resonant!",
            xpReward: 85,
            targetNotes: [48, 55, 48, 55],
            isPremium: true
        ),

        // Stage 4: Both Hands Coordination
        Lesson(
            id: "lesson_8_two_hand_harmony",
            stageId: "stage_4",
            stageTitleKey: "lessons_stage_both_hands",
            title: "Two-Hand Synchronization",
            description: "Combine Left Hand Bass with Right Hand Melody.",
            instruction: "Play C3 with Left Hand, then C4 with Right Hand: C3 -> C4 -> G3 -> G4.",
            xpReward: 110,
            targetNotes: [48, 60, 55, 67],
            isPremium: true
        ),

        // Stage 5: Essential Chords
        Lesson(
            id: "lesson_9_c_major_chord",
            stageId: "stage_5",
            stageTitleKey: "lessons_stage_chords",
            title: "The C Major Triad",
            description: "The most famous chord in Western harmony.",
            instruction: "Play C4, E4, and G4 in sequence to build the C Major chord: C -> E -> G.",
            xpReward: 120,
            targetNotes: [60, 64, 67],
            isPremium: true
        ),
        Lesson(
            id: "lesson_10_g_major_chord",
            stageId: "stage_5",
            stageTitleKey: "lessons_stage_chords",
            title: "The G Major Triad",
            description: "The dominant chord that always resolves back to C.",
            instruction: "Play G3, B3, and D4: G -> B -> D.",
            xpReward: 125,
            targetNotes: [55, 59, 62],
            isPremium: true
        ),

        // Stage 6: Master Songs
        Lesson(
            id: "lesson_11_canon_in_d",
            stageId: "stage_6",
            stageTitleKey: "lessons_stage_songs",
            title: "Canon in D Mastery",
            description: "Play Pachelbel's descending bassline and harmony.",
            instruction: "Play the iconic progression notes: D -> C# -> B -> A.",
            xpReward: 150,
            targetNotes: [74, 73, 71, 69],
            targetSongId: "canon_in_d",
            isPremium: true
        ),
        Lesson(
            id: "lesson_12_fur_elise_mastery",
            stageId: "stage_6",
            stageTitleKey: "lessons_stage_songs",
            title: "Für Elise Signature Hook",
            description: "Beethoven's unforgettable alternating motif.",
            instruction: "Play the famous phrase: E5 -> D#5 -> E5 -> D#5 -> E5 -> B4 -> D5 -> C5 -> A4.",
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
