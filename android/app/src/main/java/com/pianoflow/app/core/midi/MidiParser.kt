package com.pianoflow.app.core.midi

import java.io.InputStream
import java.nio.ByteBuffer

/**
 * Lightweight native Standard MIDI File (SMF 0/1) parser.
 * Reads tracks, handles variable-length quantities, tempo meta events,
 * and extracts timed Note-On / Note-Off pairs into Song tracks.
 */
object MidiParser {

    fun parse(inputStream: InputStream, title: String = "Imported Song"): Song {
        val bytes = inputStream.readBytes()
        val buffer = ByteBuffer.wrap(bytes)

        // Read MThd header
        val headerTag = ByteArray(4).also { buffer.get(it) }.toString(Charsets.US_ASCII)
        if (headerTag != "MThd") {
            throw IllegalArgumentException("Not a valid MIDI file: expected MThd, got $headerTag")
        }

        val headerLength = buffer.int
        val format = buffer.short.toInt()
        val numTracks = buffer.short.toInt()
        val division = buffer.short.toInt()

        if (headerLength > 6) {
            buffer.position(buffer.position() + (headerLength - 6))
        }

        val tracks = mutableListOf<SongTrack>()
        var defaultTempoMicrosPerQuarter = 500_000L // 120 BPM default

        for (t in 0 until numTracks) {
            val trackTag = ByteArray(4).also { buffer.get(it) }.toString(Charsets.US_ASCII)
            if (trackTag != "MTrk") break
            val trackLen = buffer.int
            val trackEndPos = buffer.position() + trackLen

            var currentTick = 0L
            var runningStatus = 0
            val activeNotes = mutableMapOf<Int, Pair<Long, Float>>() // midi -> (startTick, velocity)
            val parsedNotes = mutableListOf<MidiNoteEvent>()

            while (buffer.position() < trackEndPos) {
                val deltaTick = readVarLen(buffer)
                currentTick += deltaTick

                var status = buffer.get().toInt() and 0xFF
                if (status < 0x80) {
                    // Running status
                    buffer.position(buffer.position() - 1)
                    status = runningStatus
                } else {
                    runningStatus = status
                }

                if (status == 0xFF) {
                    // Meta event
                    val type = buffer.get().toInt() and 0xFF
                    val len = readVarLen(buffer).toInt()
                    if (type == 0x51 && len == 3) {
                        // Set Tempo
                        val b0 = buffer.get().toLong() and 0xFF
                        val b1 = buffer.get().toLong() and 0xFF
                        val b2 = buffer.get().toLong() and 0xFF
                        defaultTempoMicrosPerQuarter = (b0 shl 16) or (b1 shl 8) or b2
                    } else {
                        buffer.position(buffer.position() + len)
                    }
                } else if (status in 0xF0..0xF7) {
                    // SysEx event
                    val len = readVarLen(buffer).toInt()
                    buffer.position(buffer.position() + len)
                } else {
                    val eventType = status and 0xF0
                    when (eventType) {
                        0x90 -> { // Note On
                            val note = buffer.get().toInt() and 0xFF
                            val velocity = buffer.get().toInt() and 0xFF
                            if (velocity > 0) {
                                activeNotes[note] = Pair(currentTick, velocity / 127f)
                            } else {
                                // Note off via velocity 0
                                activeNotes.remove(note)?.let { (startTick, vel) ->
                                    val startMs = ticksToMs(startTick, division, defaultTempoMicrosPerQuarter)
                                    val durationMs = kotlin.math.max(80L, ticksToMs(currentTick - startTick, division, defaultTempoMicrosPerQuarter))
                                    parsedNotes.add(MidiNoteEvent(note, startMs, durationMs, vel, HandType.RIGHT))
                                }
                            }
                        }
                        0x80 -> { // Note Off
                            val note = buffer.get().toInt() and 0xFF
                            buffer.get() // release velocity
                            activeNotes.remove(note)?.let { (startTick, vel) ->
                                val startMs = ticksToMs(startTick, division, defaultTempoMicrosPerQuarter)
                                val durationMs = kotlin.math.max(80L, ticksToMs(currentTick - startTick, division, defaultTempoMicrosPerQuarter))
                                parsedNotes.add(MidiNoteEvent(note, startMs, durationMs, vel, HandType.RIGHT))
                            }
                        }
                        0xA0, 0xB0, 0xE0 -> {
                            buffer.get()
                            buffer.get()
                        }
                        0xC0, 0xD0 -> {
                            buffer.get()
                        }
                    }
                }
            }

            if (parsedNotes.isNotEmpty()) {
                val hand = if (tracks.isEmpty()) HandType.RIGHT else HandType.LEFT
                tracks.add(SongTrack(hand, parsedNotes.sortedBy { it.startMs }))
            }
        }

        val bpm = (60_000_000L / defaultTempoMicrosPerQuarter).toInt().coerceIn(30, 300)
        return Song(
            id = title.lowercase().replace(" ", "_"),
            title = title,
            composer = "Standard MIDI",
            difficulty = "Medium",
            bpm = bpm,
            timeSignature = "4/4",
            isPremium = false,
            tracks = if (tracks.isEmpty()) listOf(SongTrack(HandType.RIGHT, emptyList())) else tracks
        )
    }

    private fun readVarLen(buffer: ByteBuffer): Long {
        var value = 0L
        var byte: Int
        do {
            byte = buffer.get().toInt() and 0xFF
            value = (value shl 7) or (byte and 0x7F).toLong()
        } while ((byte and 0x80) != 0)
        return value
    }

    private fun ticksToMs(ticks: Long, division: Int, tempoMicrosPerQuarter: Long): Long {
        if (division <= 0) return ticks * 2L
        return (ticks * tempoMicrosPerQuarter) / (division * 1000L)
    }
}
