package com.pianoflow.app.core.audio

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin

data class RecordedNoteEvent(
    val midiNumber: Int,
    val startMs: Long,
    val durationMs: Long = 500L,
    val velocity: Float = 0.8f
)

/**
 * High-performance offline WAV audio renderer.
 * Converts a list of timestamped note events into an acoustic grand piano WAV file.
 */
object AudioWavExporter {

    private const val SAMPLE_RATE = 44100

    suspend fun exportToWav(
        context: Context,
        fileName: String,
        notes: List<RecordedNoteEvent>
    ): File = withContext(Dispatchers.IO) {
        val audioDir = File(context.cacheDir, "audio")
        if (!audioDir.exists()) audioDir.mkdirs()

        val safeName = fileName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val outputFile = File(audioDir, "$safeName.wav")

        if (notes.isEmpty()) {
            writeEmptyWav(outputFile)
            return@withContext outputFile
        }

        val totalDurationMs = notes.maxOf { it.startMs + it.durationMs } + 1000L // 1s ring out
        val totalSamples = ((totalDurationMs / 1000.0) * SAMPLE_RATE).toInt()
        val mixBuffer = FloatArray(totalSamples)

        for (note in notes) {
            val startSample = ((note.startMs / 1000.0) * SAMPLE_RATE).toInt()
            val noteDurationSamples = ((note.durationMs / 1000.0) * SAMPLE_RATE).toInt()
            val noteSamples = max(noteDurationSamples, (SAMPLE_RATE * 1.5).toInt())

            val freq = NotePitch.ALL_NOTES.find { it.midiNumber == note.midiNumber }?.frequencyHz ?: 440f
            val decay = 2.0f + (note.midiNumber - 36) * 0.04f

            for (i in 0 until noteSamples) {
                val mixIndex = startSample + i
                if (mixIndex >= totalSamples) break

                val t = i.toFloat() / SAMPLE_RATE
                val envelope = exp(-decay * t)

                var s = 0.65f * sin(2.0 * PI * freq * t).toFloat()
                s += 0.25f * sin(2.0 * PI * 2 * freq * t).toFloat()
                s += 0.15f * sin(2.0 * PI * 3 * freq * t).toFloat()
                s += 0.08f * sin(2.0 * PI * 4 * freq * t).toFloat()

                mixBuffer[mixIndex] += s * envelope * note.velocity * 0.35f
            }
        }

        // Convert normalized float to 16-bit signed PCM
        val pcmData = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val clamped = mixBuffer[i].coerceIn(-1.0f, 1.0f)
            pcmData[i] = (clamped * 32767).toInt().toShort()
        }

        FileOutputStream(outputFile).use { fos ->
            val pcmBytes = pcmData.size * 2
            writeWavHeader(fos, pcmBytes, SAMPLE_RATE, 1, 16)
            val byteBuffer = ByteBuffer.allocate(pcmBytes).order(ByteOrder.LITTLE_ENDIAN)
            pcmData.forEach { byteBuffer.putShort(it) }
            fos.write(byteBuffer.array())
        }

        outputFile
    }

    private fun writeEmptyWav(outputFile: File) {
        FileOutputStream(outputFile).use { fos ->
            writeWavHeader(fos, 0, SAMPLE_RATE, 1, 16)
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
        bb.putInt(16)
        bb.putShort(1)
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
