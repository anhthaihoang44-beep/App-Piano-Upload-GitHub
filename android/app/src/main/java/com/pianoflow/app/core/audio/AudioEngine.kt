package com.pianoflow.app.core.audio

import kotlinx.coroutines.flow.StateFlow

/**
 * Clean audio engine interface for low-latency polyphonic piano playback.
 * Allows swapping between SoundPool implementation and Oboe/AAudio high-performance C++ backend.
 */
interface AudioEngine {
    val isLoaded: StateFlow<Boolean>
    val loadingProgress: StateFlow<Float>

    suspend fun initialize()
    fun playNote(midiNumber: Int, velocity: Float = 1.0f): Int
    fun stopNote(midiNumber: Int)
    fun stopAllNotes()
    fun setSustain(enabled: Boolean)
    fun isSustainEnabled(): Boolean
    fun setVolume(volume: Float)
    fun getVolume(): Float
    fun release()
}
