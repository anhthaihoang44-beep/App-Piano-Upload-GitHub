package com.pianoflow.app

import com.pianoflow.app.feature.lessons.model.LessonCurriculum
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonProgressionTest {

    @Test
    fun testCurriculumSetup() {
        val allLessons = LessonCurriculum.ALL_LESSONS
        assertEquals(12, allLessons.size)

        // First 5 lessons are free
        val freeLessons = allLessons.filter { !it.isPremium }
        assertEquals(5, freeLessons.size)

        // All lessons have target notes
        allLessons.forEach { lesson ->
            assertTrue(lesson.targetNotes.isNotEmpty())
            assertTrue(lesson.xpReward > 0)
        }
    }

    @Test
    fun testLessonUnlockLogic() {
        val completedLessonIds = mutableSetOf<String>()
        val allLessons = LessonCurriculum.ALL_LESSONS

        fun isLessonUnlocked(index: Int): Boolean {
            if (index == 0) return true
            val prevLesson = allLessons[index - 1]
            return prevLesson.id in completedLessonIds
        }

        // Lesson 0 (first lesson) is unlocked initially
        assertTrue(isLessonUnlocked(0))
        // Lesson 1 is locked initially
        assertFalse(isLessonUnlocked(1))

        // Complete lesson 0
        completedLessonIds.add(allLessons[0].id)
        assertTrue(isLessonUnlocked(1))
        assertFalse(isLessonUnlocked(2))

        // Complete lesson 1
        completedLessonIds.add(allLessons[1].id)
        assertTrue(isLessonUnlocked(2))
    }
}
