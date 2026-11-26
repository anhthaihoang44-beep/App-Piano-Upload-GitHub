package com.pianoflow.app

import com.pianoflow.app.core.util.MonotonicClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimingEngineTest {

    @Test
    fun testClockStartAndPause() {
        val clock = MonotonicClock()
        assertFalse(clock.isPlaying)
        assertEquals(0L, clock.currentPositionMs())

        clock.start(100L)
        assertTrue(clock.isPlaying)
        Thread.sleep(50)
        val posAfterSleep = clock.currentPositionMs()
        assertTrue("Position should advance: $posAfterSleep", posAfterSleep >= 140L)

        clock.pause()
        assertFalse(clock.isPlaying)
        val pausedPos = clock.currentPositionMs()
        Thread.sleep(30)
        assertEquals(pausedPos, clock.currentPositionMs())

        clock.resume()
        assertTrue(clock.isPlaying)
        Thread.sleep(30)
        assertTrue(clock.currentPositionMs() > pausedPos)
    }

    @Test
    fun testClockSpeedMultiplier() {
        val clock = MonotonicClock()
        clock.setSpeed(1.5f)
        assertEquals(1.5f, clock.getSpeed(), 0.01f)

        clock.start(0L)
        Thread.sleep(50)
        val elapsed = clock.currentPositionMs()
        // At 1.5x speed, 50ms should yield ~70-80ms elapsed
        assertTrue("Expected >= 60ms at 1.5x speed, got $elapsed", elapsed >= 60L)
    }

    @Test
    fun testClockSeek() {
        val clock = MonotonicClock()
        clock.start(0L)
        clock.seekTo(5000L)
        val pos = clock.currentPositionMs()
        assertTrue("Expected >= 5000ms, got $pos", pos >= 5000L)
    }
}
