package com.pianoflow.app.feature.lessons.model

import com.pianoflow.app.R

data class Lesson(
    val id: String,
    val stageId: String,
    val stageTitleRes: Int,
    val title: String,
    val description: String,
    val instruction: String,
    val xpReward: Int,
    val targetNotes: List<Int>, // Sequence of MIDI notes to play
    val targetSongId: String? = null,
    val isPremium: Boolean = false
)

object LessonCurriculum {

    val ALL_LESSONS: List<Lesson> = listOf(
        // Stage 1: Piano Basics
        Lesson(
            id = "lesson_1_middle_c",
            stageId = "stage_1",
            stageTitleRes = R.string.lessons_stage_basics,
            title = "Find Middle C",
            description = "The central home base of the piano keyboard.",
            instruction = "Middle C (C4) is located directly to the left of the group of two black keys near the center. Press Middle C to start!",
            xpReward = 50,
            targetNotes = listOf(60, 60, 60), // Press C4 three times
            isPremium = false
        ),
        Lesson(
            id = "lesson_2_c_d_e",
            stageId = "stage_1",
            stageTitleRes = R.string.lessons_stage_basics,
            title = "The First Trio: C, D, E",
            description = "Learn your first three consecutive white keys.",
            instruction = "Place your fingers on C4, D4, and E4. Play them in order from left to right: C -> D -> E.",
            xpReward = 60,
            targetNotes = listOf(60, 62, 64),
            isPremium = false
        ),
        Lesson(
            id = "lesson_3_black_keys",
            stageId = "stage_1",
            stageTitleRes = R.string.lessons_stage_basics,
            title = "Black Keys: C# and D#",
            description = "Discover sharps and the distinctive twin black keys.",
            instruction = "The two black keys above C and D are C# (sharp) and D#. Play C4, C#4, D4, D#4.",
            xpReward = 75,
            targetNotes = listOf(60, 61, 62, 63),
            isPremium = false
        ),

        // Stage 2: Right Hand Technique
        Lesson(
            id = "lesson_4_five_fingers",
            stageId = "stage_2",
            stageTitleRes = R.string.lessons_stage_right_hand,
            title = "5-Finger Pentascale (C–G)",
            description = "Use all five fingers on one position.",
            instruction = "Assign Thumb (1) to C, Index (2) to D, Middle (3) to E, Ring (4) to F, Pinky (5) to G. Play C -> D -> E -> F -> G.",
            xpReward = 80,
            targetNotes = listOf(60, 62, 64, 65, 67),
            isPremium = false
        ),
        Lesson(
            id = "lesson_5_ode_to_joy_theme",
            stageId = "stage_2",
            stageTitleRes = R.string.lessons_stage_right_hand,
            title = "Ode to Joy Theme",
            description = "Play Beethoven's beloved melody using the 5-finger position.",
            instruction = "Play the opening phrase: E -> E -> F -> G -> G -> F -> E -> D.",
            xpReward = 100,
            targetNotes = listOf(64, 64, 65, 67, 67, 65, 64, 62),
            targetSongId = "ode_to_joy",
            isPremium = false
        ),

        // Stage 3: Left Hand & Bass
        Lesson(
            id = "lesson_6_bass_c",
            stageId = "stage_3",
            stageTitleRes = R.string.lessons_stage_left_hand,
            title = "Bass C & Low Register",
            description = "Anchor your left hand an octave lower.",
            instruction = "Look one octave to the left for Bass C (C3). Press C3 with your left hand pinky.",
            xpReward = 75,
            targetNotes = listOf(48, 48, 48),
            isPremium = true
        ),
        Lesson(
            id = "lesson_7_bass_fifth",
            stageId = "stage_3",
            stageTitleRes = R.string.lessons_stage_left_hand,
            title = "The Power of the 5th (C & G)",
            description = "The foundational accompaniment interval in modern music.",
            instruction = "Play Bass C (C3) followed by Bass G (G3): low and resonant!",
            xpReward = 85,
            targetNotes = listOf(48, 55, 48, 55),
            isPremium = true
        ),

        // Stage 4: Both Hands Coordination
        Lesson(
            id = "lesson_8_two_hand_harmony",
            stageId = "stage_4",
            stageTitleRes = R.string.lessons_stage_both_hands,
            title = "Two-Hand Synchronization",
            description = "Combine Left Hand Bass with Right Hand Melody.",
            instruction = "Play C3 with Left Hand, then C4 with Right Hand: C3 -> C4 -> G3 -> G4.",
            xpReward = 110,
            targetNotes = listOf(48, 60, 55, 67),
            isPremium = true
        ),

        // Stage 5: Essential Chords
        Lesson(
            id = "lesson_9_c_major_chord",
            stageId = "stage_5",
            stageTitleRes = R.string.lessons_stage_chords,
            title = "The C Major Triad",
            description = "The most famous chord in Western harmony.",
            instruction = "Play C4, E4, and G4 in sequence to build the C Major chord: C -> E -> G.",
            xpReward = 120,
            targetNotes = listOf(60, 64, 67),
            isPremium = true
        ),
        Lesson(
            id = "lesson_10_g_major_chord",
            stageId = "stage_5",
            stageTitleRes = R.string.lessons_stage_chords,
            title = "The G Major Triad",
            description = "The dominant chord that always resolves back to C.",
            instruction = "Play G3, B3, and D4: G -> B -> D.",
            xpReward = 125,
            targetNotes = listOf(55, 59, 62),
            isPremium = true
        ),

        // Stage 6: Master Songs
        Lesson(
            id = "lesson_11_canon_in_d",
            stageId = "stage_6",
            stageTitleRes = R.string.lessons_stage_songs,
            title = "Canon in D Mastery",
            description = "Play Pachelbel's descending bassline and harmony.",
            instruction = "Play the iconic progression notes: D -> C# -> B -> A.",
            xpReward = 150,
            targetNotes = listOf(74, 73, 71, 69),
            targetSongId = "canon_in_d",
            isPremium = true
        ),
        Lesson(
            id = "lesson_12_fur_elise_mastery",
            stageId = "stage_6",
            stageTitleRes = R.string.lessons_stage_songs,
            title = "Für Elise Signature Hook",
            description = "Beethoven's unforgettable alternating motif.",
            instruction = "Play the famous phrase: E5 -> D#5 -> E5 -> D#5 -> E5 -> B4 -> D5 -> C5 -> A4.",
            xpReward = 200,
            targetNotes = listOf(76, 75, 76, 75, 76, 71, 74, 72, 69),
            targetSongId = "fur_elise",
            isPremium = true
        )
    )

    fun getById(id: String): Lesson? = ALL_LESSONS.find { it.id == id }
}
