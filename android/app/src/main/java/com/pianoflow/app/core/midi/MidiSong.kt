package com.pianoflow.app.core.midi

import org.json.JSONObject

enum class HandType {
    RIGHT,
    LEFT,
    BOTH;

    companion object {
        fun fromString(str: String): HandType = when (str.lowercase()) {
            "left" -> LEFT
            "both" -> BOTH
            else -> RIGHT
        }
    }
}

data class MidiNoteEvent(
    val midi: Int,
    val startMs: Long,
    val durationMs: Long,
    val velocity: Float = 0.8f,
    val hand: HandType = HandType.RIGHT
)

data class SongTrack(
    val hand: HandType,
    val notes: List<MidiNoteEvent>
)

data class Song(
    val id: String,
    val title: String,
    val composer: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val bpm: Int,
    val timeSignature: String,
    val isPremium: Boolean = false,
    val tracks: List<SongTrack>
) {
    val allNotes: List<MidiNoteEvent> by lazy {
        tracks.flatMap { track ->
            track.notes.map { it.copy(hand = track.hand) }
        }.sortedBy { it.startMs }
    }

    val durationMs: Long by lazy {
        allNotes.maxOfOrNull { it.startMs + it.durationMs } ?: 0L
    }

    companion object {
        fun fromJson(jsonStr: String): Song {
            val json = JSONObject(jsonStr)
            val id = json.optString("id", "")
            val title = json.getString("title")
            val composer = json.optString("composer", "Traditional")
            val difficulty = json.optString("difficulty", "Easy")
            val bpm = json.optInt("bpm", 120)
            val timeSignature = json.optString("timeSignature", "4/4")
            val isPremium = json.optBoolean("isPremium", false)

            val tracksArray = json.optJSONArray("tracks") ?: org.json.JSONArray()
            val tracks = mutableListOf<SongTrack>()

            for (i in 0 until tracksArray.length()) {
                val trackObj = tracksArray.getJSONObject(i)
                val hand = HandType.fromString(trackObj.optString("hand", "Right"))
                val notesArray = trackObj.optJSONArray("notes") ?: org.json.JSONArray()
                val notes = mutableListOf<MidiNoteEvent>()

                for (j in 0 until notesArray.length()) {
                    val noteObj = notesArray.getJSONObject(j)
                    notes.add(
                        MidiNoteEvent(
                            midi = noteObj.getInt("midi"),
                            startMs = noteObj.getLong("startMs"),
                            durationMs = noteObj.optLong("durationMs", 500L),
                            velocity = noteObj.optDouble("velocity", 0.8).toFloat(),
                            hand = hand
                        )
                    )
                }
                tracks.add(SongTrack(hand, notes))
            }

            return Song(
                id = id.ifEmpty { title.lowercase().replace(" ", "_") },
                title = title,
                composer = composer,
                difficulty = difficulty,
                bpm = bpm,
                timeSignature = timeSignature,
                isPremium = isPremium,
                tracks = tracks
            )
        }
    }
}
