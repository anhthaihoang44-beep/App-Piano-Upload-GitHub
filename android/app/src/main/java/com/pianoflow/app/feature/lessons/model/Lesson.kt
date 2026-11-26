package com.pianoflow.app.feature.lessons.model

import androidx.annotation.StringRes
import com.pianoflow.app.R

data class Lesson(
    val id: String,
    val stageId: String,
    @StringRes val stageTitleRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val instructionRes: Int,
    val xpReward: Int,
    val targetNotes: List<Int>,
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
            titleRes = R.string.lesson_1_title,
            descriptionRes = R.string.lesson_1_desc,
            instructionRes = R.string.lesson_1_inst,
            xpReward = 50,
            targetNotes = listOf(60, 60, 60),
            isPremium = false
        ),
        Lesson(
            id = "lesson_2_c_d_e",
            stageId = "stage_1",
            stageTitleRes = R.string.lessons_stage_basics,
            titleRes = R.string.lesson_2_title,
            descriptionRes = R.string.lesson_2_desc,
            instructionRes = R.string.lesson_2_inst,
            xpReward = 60,
            targetNotes = listOf(60, 62, 64),
            isPremium = false
        ),
        Lesson(
            id = "lesson_3_black_keys",
            stageId = "stage_1",
            stageTitleRes = R.string.lessons_stage_basics,
            titleRes = R.string.lesson_3_title,
            descriptionRes = R.string.lesson_3_desc,
            instructionRes = R.string.lesson_3_inst,
            xpReward = 75,
            targetNotes = listOf(60, 61, 62, 63),
            isPremium = false
        ),

        // Stage 2: Right Hand Technique
        Lesson(
            id = "lesson_4_five_fingers",
            stageId = "stage_2",
            stageTitleRes = R.string.lessons_stage_right_hand,
            titleRes = R.string.lesson_4_title,
            descriptionRes = R.string.lesson_4_desc,
            instructionRes = R.string.lesson_4_inst,
            xpReward = 80,
            targetNotes = listOf(60, 62, 64, 65, 67),
            isPremium = false
        ),
        Lesson(
            id = "lesson_5_ode_to_joy_theme",
            stageId = "stage_2",
            stageTitleRes = R.string.lessons_stage_right_hand,
            titleRes = R.string.lesson_5_title,
            descriptionRes = R.string.lesson_5_desc,
            instructionRes = R.string.lesson_5_inst,
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
            titleRes = R.string.lesson_6_title,
            descriptionRes = R.string.lesson_6_desc,
            instructionRes = R.string.lesson_6_inst,
            xpReward = 75,
            targetNotes = listOf(48, 48, 48),
            isPremium = true
        ),
        Lesson(
            id = "lesson_7_bass_fifth",
            stageId = "stage_3",
            stageTitleRes = R.string.lessons_stage_left_hand,
            titleRes = R.string.lesson_7_title,
            descriptionRes = R.string.lesson_7_desc,
            instructionRes = R.string.lesson_7_inst,
            xpReward = 85,
            targetNotes = listOf(48, 55, 48, 55),
            isPremium = true
        ),

        // Stage 4: Both Hands Coordination
        Lesson(
            id = "lesson_8_two_hand_harmony",
            stageId = "stage_4",
            stageTitleRes = R.string.lessons_stage_both_hands,
            titleRes = R.string.lesson_8_title,
            descriptionRes = R.string.lesson_8_desc,
            instructionRes = R.string.lesson_8_inst,
            xpReward = 110,
            targetNotes = listOf(48, 60, 55, 67),
            isPremium = true
        ),

        // Stage 5: Essential Chords
        Lesson(
            id = "lesson_9_c_major_chord",
            stageId = "stage_5",
            stageTitleRes = R.string.lessons_stage_chords,
            titleRes = R.string.lesson_9_title,
            descriptionRes = R.string.lesson_9_desc,
            instructionRes = R.string.lesson_9_inst,
            xpReward = 120,
            targetNotes = listOf(60, 64, 67),
            isPremium = true
        ),
        Lesson(
            id = "lesson_10_g_major_chord",
            stageId = "stage_5",
            stageTitleRes = R.string.lessons_stage_chords,
            titleRes = R.string.lesson_10_title,
            descriptionRes = R.string.lesson_10_desc,
            instructionRes = R.string.lesson_10_inst,
            xpReward = 125,
            targetNotes = listOf(55, 59, 62),
            isPremium = true
        ),

        // Stage 6: Master Songs
        Lesson(
            id = "lesson_11_canon_in_d",
            stageId = "stage_6",
            stageTitleRes = R.string.lessons_stage_songs,
            titleRes = R.string.lesson_11_title,
            descriptionRes = R.string.lesson_11_desc,
            instructionRes = R.string.lesson_11_inst,
            xpReward = 150,
            targetNotes = listOf(74, 73, 71, 69),
            targetSongId = "canon_in_d",
            isPremium = true
        ),
        Lesson(
            id = "lesson_12_fur_elise_mastery",
            stageId = "stage_6",
            stageTitleRes = R.string.lessons_stage_songs,
            titleRes = R.string.lesson_12_title,
            descriptionRes = R.string.lesson_12_desc,
            instructionRes = R.string.lesson_12_inst,
            xpReward = 200,
            targetNotes = listOf(76, 75, 76, 75, 76, 71, 74, 72, 69),
            targetSongId = "fur_elise",
            isPremium = true
        )
    )

    fun getById(id: String): Lesson? = ALL_LESSONS.find { it.id == id }
}
