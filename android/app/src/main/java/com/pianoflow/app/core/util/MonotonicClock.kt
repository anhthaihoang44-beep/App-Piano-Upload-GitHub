package com.pianoflow.app.core.util

/**
 * High-precision monotonic clock for the piano playback and falling notes rendering engine.
 * Never uses Handler.postDelayed or Wall-clock System.currentTimeMillis() which are prone to jitter/drift.
 * Accurately tracks song progress in milliseconds with speed multiplier and pause/resume logic.
 */
class MonotonicClock {

    private var startNanoTime: Long = 0L
    private var pausedPositionMs: Long = 0L
    private var isRunning: Boolean = false
    private var speedMultiplier: Float = 1.0f

    val isPlaying: Boolean get() = isRunning

    fun start(fromPositionMs: Long = 0L) {
        pausedPositionMs = fromPositionMs
        startNanoTime = System.nanoTime()
        isRunning = true
    }

    fun pause() {
        if (!isRunning) return
        pausedPositionMs = currentPositionMs()
        isRunning = false
    }

    fun resume() {
        if (isRunning) return
        startNanoTime = System.nanoTime()
        isRunning = true
    }

    fun seekTo(positionMs: Long) {
        pausedPositionMs = positionMs
        if (isRunning) {
            startNanoTime = System.nanoTime()
        }
    }

    fun setSpeed(speed: Float) {
        if (isRunning) {
            // Snapshot current progress at old speed before changing
            pausedPositionMs = currentPositionMs()
            startNanoTime = System.nanoTime()
        }
        speedMultiplier = speed.coerceIn(0.25f, 2.5f)
    }

    fun getSpeed(): Float = speedMultiplier

    fun currentPositionMs(): Long {
        if (!isRunning) return pausedPositionMs
        val elapsedNanos = System.nanoTime() - startNanoTime
        val elapsedMs = (elapsedNanos / 1_000_000L) * speedMultiplier
        return pausedPositionMs + elapsedMs.toLong()
    }

    fun reset() {
        isRunning = false
        pausedPositionMs = 0L
        startNanoTime = 0L
    }
}
