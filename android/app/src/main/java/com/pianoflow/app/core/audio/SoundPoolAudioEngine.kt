package com.pianoflow.app.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-performance, low-latency SoundPool-based audio engine.
 * Preloads synthesized acoustic piano waveforms spanning all common octaves (C2..C7).
 * Provides polyphonic playback with sustain pedal logic and sub-50ms latency.
 */
class SoundPoolAudioEngine(private val context: Context) : AudioEngine {

    private val tag = "SoundPoolAudioEngine"
    private var soundPool: SoundPool? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isLoaded = MutableStateFlow(false)
    override val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0f)
    override val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()

    private val soundMap = mutableMapOf<Int, Int>() // midiNumber -> soundId
    private val activeStreams = mutableMapOf<Int, MutableList<Int>>() // midiNumber -> streamIds
    private var sustainEnabled = false
    private var masterVolume = 1.0f

    companion object {
        const val MAX_STREAMS = 32
        const val MIN_MIDI = 36 // C2
        const val MAX_MIDI = 96 // C7
        const val SAMPLE_RATE = 44100
        const val DURATION_SECONDS = 1.8f
    }

    override suspend fun initialize() {
        if (_isLoaded.value) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
            .build()

        val sp = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool = sp

        val totalNotes = MAX_MIDI - MIN_MIDI + 1
        var loadedCount = 0

        sp.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedCount++
                _loadingProgress.value = loadedCount.toFloat() / totalNotes
                if (loadedCount >= totalNotes) {
                    _isLoaded.value = true
                    Log.d(tag, "All piano samples preloaded successfully: $loadedCount notes")
                }
            } else {
                Log.w(tag, "Failed to load sample ID: $sampleId, status: $status")
            }
        }

        // Generate and load cached high-fidelity acoustic piano waveforms
        scope.launch(Dispatchers.IO) {
            val cacheDir = File(context.cacheDir, "piano_samples")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            for (midi in MIN_MIDI..MAX_MIDI) {
                val sampleFile = File(cacheDir, "piano_$midi.wav")
                if (!sampleFile.exists() || sampleFile.length() == 0L) {
                    generateAcousticPianoSample(midi, sampleFile)
                }
                val soundId = sp.load(sampleFile.absolutePath, 1)
                soundMap[midi] = soundId
            }
        }
    }

    override fun playNote(midiNumber: Int, velocity: Float): Int {
        val sp = soundPool ?: return 0
        val soundId = soundMap[midiNumber] ?: run {
            // Find closest available note and pitch-shift if beyond range
            val closestMidi = soundMap.keys.minByOrNull { kotlin.math.abs(it - midiNumber) } ?: return 0
            val rate = NotePitch.ALL_NOTES.find { it.midiNumber == midiNumber }?.frequencyHz?.div(
                NotePitch.ALL_NOTES.find { it.midiNumber == closestMidi }?.frequencyHz ?: 440f
            ) ?: 1.0f
            val streamId = sp.play(soundMap[closestMidi] ?: return 0, masterVolume * velocity, masterVolume * velocity, 1, 0, rate.coerceIn(0.5f, 2.0f))
            return streamId
        }

        val vol = (masterVolume * velocity.coerceIn(0.1f, 1.0f)).coerceIn(0f, 1f)
        val streamId = sp.play(soundId, vol, vol, 1, 0, 1.0f)
        if (streamId != 0) {
            synchronized(activeStreams) {
                val list = activeStreams.getOrPut(midiNumber) { mutableListOf() }
                list.add(streamId)
                if (list.size > 4) list.removeAt(0)
            }
        }
        return streamId
    }

    override fun stopNote(midiNumber: Int) {
        if (sustainEnabled) return // When sustain pedal is pressed, allow sound to decay naturally
        val sp = soundPool ?: return
        synchronized(activeStreams) {
            activeStreams[midiNumber]?.forEach { streamId ->
                sp.stop(streamId)
            }
            activeStreams[midiNumber]?.clear()
        }
    }

    override fun stopAllNotes() {
        val sp = soundPool ?: return
        synchronized(activeStreams) {
            activeStreams.values.flatten().forEach { streamId ->
                sp.stop(streamId)
            }
            activeStreams.clear()
        }
    }

    override fun setSustain(enabled: Boolean) {
        sustainEnabled = enabled
        if (!enabled) {
            // Sustain released: stop all lingering voices
            stopAllNotes()
        }
    }

    override fun isSustainEnabled(): Boolean = sustainEnabled

    override fun setVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        val sp = soundPool ?: return
        synchronized(activeStreams) {
            activeStreams.values.flatten().forEach { streamId ->
                sp.setVolume(streamId, masterVolume, masterVolume)
            }
        }
    }

    override fun getVolume(): Float = masterVolume

    override fun release() {
        stopAllNotes()
        soundPool?.release()
        soundPool = null
        soundMap.clear()
        _isLoaded.value = false
    }

    /**
     * Synthesizes an acoustic piano sample with fundamental frequency + 6 harmonics,
     * physical hammer strike click, and natural exponential acoustic envelope decay.
     */
    private fun generateAcousticPianoSample(midiNumber: Int, outputFile: File) {
        val freq = NotePitch.ALL_NOTES.find { it.midiNumber == midiNumber }?.frequencyHz ?: 440.0f
        val numSamples = (SAMPLE_RATE * DURATION_SECONDS).toInt()
        val pcmData = ShortArray(numSamples)

        val decayRate = (2.2f + (midiNumber - 36) * 0.05f) // Higher notes decay faster

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = exp(-decayRate * t)

            // Attack hammer click for the first 8ms
            val hammerNoise = if (t < 0.008f) {
                ((Math.random() - 0.5) * exp(-t * 600.0) * 0.35).toFloat()
            } else 0f

            // Harmonic series of an acoustic grand piano string
            var sample = 0.65f * sin(2.0 * PI * freq * t).toFloat()
            sample += 0.25f * sin(2.0 * PI * 2 * freq * t).toFloat()
            sample += 0.15f * sin(2.0 * PI * 3 * freq * t).toFloat()
            sample += 0.08f * sin(2.0 * PI * 4 * freq * t).toFloat()
            sample += 0.04f * sin(2.0 * PI * 5 * freq * t).toFloat()
            sample += 0.02f * sin(2.0 * PI * 6 * freq * t).toFloat()

            // Combine harmonics, envelope, and initial hammer impact
            val total = ((sample + hammerNoise) * envelope).coerceIn(-1.0f, 1.0f)
            pcmData[i] = (total * 32767).toInt().toShort()
        }

        // Write standard 16-bit PCM WAV file
        FileOutputStream(outputFile).use { fos ->
            writeWavHeader(fos, numSamples * 2, SAMPLE_RATE, 1, 16)
            val byteBuffer = ByteBuffer.allocate(pcmData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            pcmData.forEach { byteBuffer.putShort(it) }
            fos.write(byteBuffer.array())
        }
    }

    private fun writeWavHeader(out: FileOutputStream, pcmDataLength: Int, sampleRate: Int, channels: Int, bitsPerSample: Int) {
        val totalDataLen = pcmDataLength + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteArray(44)
        val bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        bb.put("RIFF".toByteArray())
        bb.putInt(totalDataLen)
        bb.put("WAVE".toByteArray())
        bb.put("fmt ".toByteArray())
        bb.putInt(16) // Subchunk1Size for PCM
        bb.putShort(1) // AudioFormat 1 = PCM
        bb.putShort(channels.toShort())
        bb.putInt(sampleRate)
        bb.putInt(byteRate)
        bb.putShort((channels * bitsPerSample / 8).toShort())
        bb.putShort(bitsPerSample.toShort())
        bb.put("data".toByteArray())
        bb.putInt(pcmDataLength)

        out.write(header)
    }
}
