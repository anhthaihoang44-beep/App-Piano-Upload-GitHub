package com.pianoflow.app

import com.pianoflow.app.core.audio.NotationType
import com.pianoflow.app.core.audio.NotePitch
import com.pianoflow.app.core.midi.BundledSongs
import com.pianoflow.app.core.midi.HandType
import com.pianoflow.app.core.midi.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SongParsingTest {

    @Test
    fun testNotePitchMapping() {
        val middleC = NotePitch.fromMidi(60)
        assertNotNull(middleC)
        assertEquals("C4", middleC!!.nameEn)
        assertEquals("Do4", middleC.nameSolfege)
        assertFalse(middleC.isBlack)
        assertEquals(4, middleC.octave)

        val cSharp = NotePitch.fromMidi(61)
        assertNotNull(cSharp)
        assertEquals("C#4", cSharp!!.nameEn)
        assertTrue(cSharp.isBlack)

        val a4 = NotePitch.fromMidi(69)
        assertNotNull(a4)
        assertEquals(440f, a4!!.frequencyHz, 0.5f)

        assertEquals("C4", NotePitch.getDisplayName(60, NotationType.CDE))
        assertEquals("Do4", NotePitch.getDisplayName(60, NotationType.DO_RE_MI))
        assertEquals("", NotePitch.getDisplayName(60, NotationType.OFF))
    }

    @Test
    fun testSongFromJson() {
        val json = """
            {
              "id": "test_song",
              "title": "Test Melody",
              "composer": "Tester",
              "difficulty": "Easy",
              "bpm": 120,
              "timeSignature": "4/4",
              "isPremium": false,
              "tracks": [
                {
                  "hand": "Right",
                  "notes": [
                    {"midi": 60, "startMs": 0, "durationMs": 400},
                    {"midi": 62, "startMs": 500, "durationMs": 400}
                  ]
                },
                {
                  "hand": "Left",
                  "notes": [
                    {"midi": 48, "startMs": 0, "durationMs": 1000}
                  ]
                }
              ]
            }
        """.trimIndent()

        val song = Song.fromJson(json)
        assertEquals("test_song", song.id)
        assertEquals("Test Melody", song.title)
        assertEquals(120, song.bpm)
        assertEquals(2, song.tracks.size)

        // allNotes sorted by startMs
        val all = song.allNotes
        assertEquals(3, all.size)
        assertEquals(1000L, song.durationMs)
        assertEquals(HandType.RIGHT, song.tracks[0].hand)
        assertEquals(HandType.LEFT, song.tracks[1].hand)
    }

    @Test
    fun testBundledSongsIntegrity() {
        assertEquals(10, BundledSongs.ALL_SONGS.size)
        BundledSongs.ALL_SONGS.forEach { song ->
            assertTrue(song.title.isNotEmpty())
            assertTrue(song.bpm > 0)
            assertTrue(song.allNotes.isNotEmpty())
            assertTrue(song.durationMs > 0L)
        }
    }
}
