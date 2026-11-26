package com.pianoflow.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoringEngineTest {

    private fun calculateScore(perfect: Int, good: Int, total: Int): Pair<Float, Int> {
        val accuracy = if (total > 0) {
            ((perfect * 100f + good * 70f) / (total * 100f)) * 100f
        } else 0f

        val stars = when {
            accuracy >= 85f -> 3
            accuracy >= 70f -> 2
            accuracy >= 45f -> 1
            else -> 0
        }
        return Pair(accuracy, stars)
    }

    @Test
    fun testPerfectScoreGivesThreeStars() {
        val (accuracy, stars) = calculateScore(perfect = 20, good = 0, total = 20)
        assertEquals(100f, accuracy, 0.01f)
        assertEquals(3, stars)
    }

    @Test
    fun testMixedScoreGivesTwoStars() {
        val (accuracy, stars) = calculateScore(perfect = 10, good = 10, total = 20)
        // (1000 + 700) / 2000 = 85%
        assertEquals(85f, accuracy, 0.01f)
        assertEquals(3, stars)

        val (accuracy2, stars2) = calculateScore(perfect = 8, good = 10, total = 20)
        // (800 + 700) / 2000 = 75%
        assertEquals(75f, accuracy2, 0.01f)
        assertEquals(2, stars2)
    }

    @Test
    fun testLowScoreGivesOneOrZeroStars() {
        val (accuracy1, stars1) = calculateScore(perfect = 5, good = 5, total = 20)
        // (500 + 350) / 2000 = 42.5%
        assertEquals(42.5f, accuracy1, 0.01f)
        assertEquals(0, stars1)

        val (accuracy2, stars2) = calculateScore(perfect = 8, good = 4, total = 20)
        // (800 + 280) / 2000 = 54%
        assertEquals(54f, accuracy2, 0.01f)
        assertEquals(1, stars2)
    }
}
