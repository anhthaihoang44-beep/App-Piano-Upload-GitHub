package com.pianoflow.app.core.midi

object BundledSongs {

    val ALL_SONGS: List<Song> = listOf(
        // 1. Ode to Joy (Beethoven) - Free
        Song(
            id = "ode_to_joy",
            title = "Ode to Joy",
            composer = "Ludwig van Beethoven",
            difficulty = "Easy",
            bpm = 110,
            timeSignature = "4/4",
            isPremium = false,
            tracks = listOf(
                SongTrack(
                    hand = HandType.RIGHT,
                    notes = listOf(
                        MidiNoteEvent(64, 0, 450, 0.8f, HandType.RIGHT),    // E4
                        MidiNoteEvent(64, 500, 450, 0.8f, HandType.RIGHT),  // E4
                        MidiNoteEvent(65, 1000, 450, 0.8f, HandType.RIGHT), // F4
                        MidiNoteEvent(67, 1500, 450, 0.8f, HandType.RIGHT), // G4
                        MidiNoteEvent(67, 2000, 450, 0.8f, HandType.RIGHT), // G4
                        MidiNoteEvent(65, 2500, 450, 0.8f, HandType.RIGHT), // F4
                        MidiNoteEvent(64, 3000, 450, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(62, 3500, 450, 0.8f, HandType.RIGHT), // D4
                        MidiNoteEvent(60, 4000, 450, 0.8f, HandType.RIGHT), // C4
                        MidiNoteEvent(60, 4500, 450, 0.8f, HandType.RIGHT), // C4
                        MidiNoteEvent(62, 5000, 450, 0.8f, HandType.RIGHT), // D4
                        MidiNoteEvent(64, 5500, 450, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(64, 6000, 650, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(62, 6750, 250, 0.8f, HandType.RIGHT), // D4
                        MidiNoteEvent(62, 7000, 900, 0.8f, HandType.RIGHT)  // D4
                    )
                ),
                SongTrack(
                    hand = HandType.LEFT,
                    notes = listOf(
                        MidiNoteEvent(48, 0, 1800, 0.6f, HandType.LEFT),    // C3
                        MidiNoteEvent(48, 2000, 1800, 0.6f, HandType.LEFT), // C3
                        MidiNoteEvent(48, 4000, 1800, 0.6f, HandType.LEFT), // C3
                        MidiNoteEvent(43, 6000, 1800, 0.6f, HandType.LEFT)  // G2
                    )
                )
            )
        ),

        // 2. Twinkle Twinkle Little Star - Free
        Song(
            id = "twinkle_twinkle",
            title = "Twinkle Twinkle Little Star",
            composer = "Traditional",
            difficulty = "Easy",
            bpm = 100,
            timeSignature = "4/4",
            isPremium = false,
            tracks = listOf(
                SongTrack(
                    hand = HandType.RIGHT,
                    notes = listOf(
                        MidiNoteEvent(60, 0, 500, 0.8f, HandType.RIGHT),    // C4
                        MidiNoteEvent(60, 600, 500, 0.8f, HandType.RIGHT),  // C4
                        MidiNoteEvent(67, 1200, 500, 0.8f, HandType.RIGHT), // G4
                        MidiNoteEvent(67, 1800, 500, 0.8f, HandType.RIGHT), // G4
                        MidiNoteEvent(69, 2400, 500, 0.8f, HandType.RIGHT), // A4
                        MidiNoteEvent(69, 3000, 500, 0.8f, HandType.RIGHT), // A4
                        MidiNoteEvent(67, 3600, 1000, 0.8f, HandType.RIGHT),// G4
                        MidiNoteEvent(65, 4800, 500, 0.8f, HandType.RIGHT), // F4
                        MidiNoteEvent(65, 5400, 500, 0.8f, HandType.RIGHT), // F4
                        MidiNoteEvent(64, 6000, 500, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(64, 6600, 500, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(62, 7200, 500, 0.8f, HandType.RIGHT), // D4
                        MidiNoteEvent(62, 7800, 500, 0.8f, HandType.RIGHT), // D4
                        MidiNoteEvent(60, 8400, 1000, 0.8f, HandType.RIGHT) // C4
                    )
                ),
                SongTrack(
                    hand = HandType.LEFT,
                    notes = listOf(
                        MidiNoteEvent(48, 0, 1000, 0.6f, HandType.LEFT),
                        MidiNoteEvent(52, 1200, 1000, 0.6f, HandType.LEFT),
                        MidiNoteEvent(53, 2400, 1000, 0.6f, HandType.LEFT),
                        MidiNoteEvent(52, 3600, 1000, 0.6f, HandType.LEFT),
                        MidiNoteEvent(48, 4800, 1000, 0.6f, HandType.LEFT),
                        MidiNoteEvent(47, 6000, 1000, 0.6f, HandType.LEFT),
                        MidiNoteEvent(43, 7200, 1000, 0.6f, HandType.LEFT),
                        MidiNoteEvent(48, 8400, 1000, 0.6f, HandType.LEFT)
                    )
                )
            )
        ),

        // 3. Happy Birthday - Free
        Song(
            id = "happy_birthday",
            title = "Happy Birthday",
            composer = "Patty & Mildred Hill",
            difficulty = "Easy",
            bpm = 100,
            timeSignature = "3/4",
            isPremium = false,
            tracks = listOf(
                SongTrack(
                    hand = HandType.RIGHT,
                    notes = listOf(
                        MidiNoteEvent(60, 0, 350, 0.8f, HandType.RIGHT),    // C4
                        MidiNoteEvent(60, 400, 200, 0.8f, HandType.RIGHT),  // C4
                        MidiNoteEvent(62, 650, 600, 0.8f, HandType.RIGHT),  // D4
                        MidiNoteEvent(60, 1300, 600, 0.8f, HandType.RIGHT), // C4
                        MidiNoteEvent(65, 1950, 600, 0.8f, HandType.RIGHT), // F4
                        MidiNoteEvent(64, 2600, 1200, 0.8f, HandType.RIGHT),// E4
                        MidiNoteEvent(60, 3900, 350, 0.8f, HandType.RIGHT), // C4
                        MidiNoteEvent(60, 4300, 200, 0.8f, HandType.RIGHT), // C4
                        MidiNoteEvent(62, 4550, 600, 0.8f, HandType.RIGHT), // D4
                        MidiNoteEvent(60, 5200, 600, 0.8f, HandType.RIGHT), // C4
                        MidiNoteEvent(67, 5850, 600, 0.8f, HandType.RIGHT), // G4
                        MidiNoteEvent(65, 6500, 1200, 0.8f, HandType.RIGHT) // F4
                    )
                )
            )
        ),

        // 4. Jingle Bells - Free
        Song(
            id = "jingle_bells",
            title = "Jingle Bells",
            composer = "James Pierpont",
            difficulty = "Easy",
            bpm = 120,
            timeSignature = "4/4",
            isPremium = false,
            tracks = listOf(
                SongTrack(
                    hand = HandType.RIGHT,
                    notes = listOf(
                        MidiNoteEvent(64, 0, 400, 0.8f, HandType.RIGHT),    // E4
                        MidiNoteEvent(64, 500, 400, 0.8f, HandType.RIGHT),  // E4
                        MidiNoteEvent(64, 1000, 800, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(64, 2000, 400, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(64, 2500, 400, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(64, 3000, 800, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(64, 4000, 400, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(67, 4500, 400, 0.8f, HandType.RIGHT), // G4
                        MidiNoteEvent(60, 5000, 600, 0.8f, HandType.RIGHT), // C4
                        MidiNoteEvent(62, 5700, 250, 0.8f, HandType.RIGHT), // D4
                        MidiNoteEvent(64, 6000, 1200, 0.8f, HandType.RIGHT) // E4
                    )
                )
            )
        ),

        // 5. Für Elise - Premium
        Song(
            id = "fur_elise",
            title = "Für Elise",
            composer = "Ludwig van Beethoven",
            difficulty = "Intermediate",
            bpm = 130,
            timeSignature = "3/8",
            isPremium = true,
            tracks = listOf(
                SongTrack(
                    hand = HandType.RIGHT,
                    notes = listOf(
                        MidiNoteEvent(76, 0, 230, 0.85f, HandType.RIGHT),   // E5
                        MidiNoteEvent(75, 250, 230, 0.85f, HandType.RIGHT),  // D#5
                        MidiNoteEvent(76, 500, 230, 0.85f, HandType.RIGHT),  // E5
                        MidiNoteEvent(75, 750, 230, 0.85f, HandType.RIGHT),  // D#5
                        MidiNoteEvent(76, 1000, 230, 0.85f, HandType.RIGHT), // E5
                        MidiNoteEvent(71, 1250, 230, 0.85f, HandType.RIGHT), // B4
                        MidiNoteEvent(74, 1500, 230, 0.85f, HandType.RIGHT), // D5
                        MidiNoteEvent(72, 1750, 230, 0.85f, HandType.RIGHT), // C5
                        MidiNoteEvent(69, 2000, 600, 0.85f, HandType.RIGHT), // A4
                        MidiNoteEvent(60, 2750, 230, 0.75f, HandType.RIGHT), // C4
                        MidiNoteEvent(64, 3000, 230, 0.75f, HandType.RIGHT), // E4
                        MidiNoteEvent(69, 3250, 230, 0.75f, HandType.RIGHT), // A4
                        MidiNoteEvent(71, 3500, 600, 0.85f, HandType.RIGHT)  // B4
                    )
                ),
                SongTrack(
                    hand = HandType.LEFT,
                    notes = listOf(
                        MidiNoteEvent(45, 2000, 800, 0.65f, HandType.LEFT), // A2
                        MidiNoteEvent(52, 2300, 600, 0.65f, HandType.LEFT), // E3
                        MidiNoteEvent(57, 2600, 600, 0.65f, HandType.LEFT), // A3
                        MidiNoteEvent(40, 3500, 800, 0.65f, HandType.LEFT)  // E2
                    )
                )
            )
        ),

        // 6. Canon in D (Pachelbel) - Premium
        Song(
            id = "canon_in_d",
            title = "Canon in D",
            composer = "Johann Pachelbel",
            difficulty = "Intermediate",
            bpm = 75,
            timeSignature = "4/4",
            isPremium = true,
            tracks = listOf(
                SongTrack(
                    hand = HandType.RIGHT,
                    notes = listOf(
                        MidiNoteEvent(74, 0, 750, 0.8f, HandType.RIGHT),    // D5
                        MidiNoteEvent(73, 800, 750, 0.8f, HandType.RIGHT),  // C#5
                        MidiNoteEvent(71, 1600, 750, 0.8f, HandType.RIGHT), // B4
                        MidiNoteEvent(69, 2400, 750, 0.8f, HandType.RIGHT), // A4
                        MidiNoteEvent(67, 3200, 750, 0.8f, HandType.RIGHT), // G4
                        MidiNoteEvent(66, 4000, 750, 0.8f, HandType.RIGHT), // F#4
                        MidiNoteEvent(67, 4800, 750, 0.8f, HandType.RIGHT), // G4
                        MidiNoteEvent(69, 5600, 750, 0.8f, HandType.RIGHT)  // A4
                    )
                ),
                SongTrack(
                    hand = HandType.LEFT,
                    notes = listOf(
                        MidiNoteEvent(50, 0, 750, 0.7f, HandType.LEFT),    // D3
                        MidiNoteEvent(45, 800, 750, 0.7f, HandType.LEFT),   // A2
                        MidiNoteEvent(47, 1600, 750, 0.7f, HandType.LEFT),  // B2
                        MidiNoteEvent(42, 2400, 750, 0.7f, HandType.LEFT),  // F#2
                        MidiNoteEvent(43, 3200, 750, 0.7f, HandType.LEFT),  // G2
                        MidiNoteEvent(38, 4000, 750, 0.7f, HandType.LEFT),  // D2
                        MidiNoteEvent(43, 4800, 750, 0.7f, HandType.LEFT),  // G2
                        MidiNoteEvent(45, 5600, 750, 0.7f, HandType.LEFT)   // A2
                    )
                )
            )
        ),

        // 7. Greensleeves - Premium
        Song(
            id = "greensleeves",
            title = "Greensleeves",
            composer = "Traditional English",
            difficulty = "Medium",
            bpm = 96,
            timeSignature = "6/8",
            isPremium = true,
            tracks = listOf(
                SongTrack(
                    hand = HandType.RIGHT,
                    notes = listOf(
                        MidiNoteEvent(69, 0, 550, 0.8f, HandType.RIGHT),    // A4
                        MidiNoteEvent(72, 625, 900, 0.8f, HandType.RIGHT),  // C5
                        MidiNoteEvent(74, 1600, 450, 0.8f, HandType.RIGHT), // D5
                        MidiNoteEvent(76, 2100, 700, 0.8f, HandType.RIGHT), // E5
                        MidiNoteEvent(77, 2850, 300, 0.8f, HandType.RIGHT), // F5
                        MidiNoteEvent(76, 3200, 700, 0.8f, HandType.RIGHT), // E5
                        MidiNoteEvent(74, 4000, 900, 0.8f, HandType.RIGHT)  // D5
                    )
                )
            )
        ),

        // 8. Minuet in G (Bach) - Premium
        Song(
            id = "minuet_in_g",
            title = "Minuet in G Major",
            composer = "Christian Petzold / J.S. Bach",
            difficulty = "Intermediate",
            bpm = 120,
            timeSignature = "3/4",
            isPremium = true,
            tracks = listOf(
                SongTrack(
                    hand = HandType.RIGHT,
                    notes = listOf(
                        MidiNoteEvent(74, 0, 480, 0.8f, HandType.RIGHT),    // D5
                        MidiNoteEvent(67, 500, 240, 0.8f, HandType.RIGHT),  // G4
                        MidiNoteEvent(69, 750, 240, 0.8f, HandType.RIGHT),  // A4
                        MidiNoteEvent(71, 1000, 240, 0.8f, HandType.RIGHT), // B4
                        MidiNoteEvent(72, 1250, 240, 0.8f, HandType.RIGHT), // C5
                        MidiNoteEvent(74, 1500, 480, 0.8f, HandType.RIGHT), // D5
                        MidiNoteEvent(67, 2000, 480, 0.8f, HandType.RIGHT), // G4
                        MidiNoteEvent(67, 2500, 480, 0.8f, HandType.RIGHT)  // G4
                    )
                )
            )
        ),

        // 9. Brahms' Lullaby - Premium
        Song(
            id = "brahms_lullaby",
            title = "Brahms' Lullaby",
            composer = "Johannes Brahms",
            difficulty = "Easy",
            bpm = 85,
            timeSignature = "3/4",
            isPremium = true,
            tracks = listOf(
                SongTrack(
                    hand = HandType.RIGHT,
                    notes = listOf(
                        MidiNoteEvent(64, 0, 450, 0.8f, HandType.RIGHT),    // E4
                        MidiNoteEvent(64, 500, 450, 0.8f, HandType.RIGHT),  // E4
                        MidiNoteEvent(67, 1000, 900, 0.8f, HandType.RIGHT), // G4
                        MidiNoteEvent(64, 2000, 450, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(64, 2500, 450, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(67, 3000, 900, 0.8f, HandType.RIGHT), // G4
                        MidiNoteEvent(64, 4000, 450, 0.8f, HandType.RIGHT), // E4
                        MidiNoteEvent(67, 4500, 450, 0.8f, HandType.RIGHT), // G4
                        MidiNoteEvent(72, 5000, 600, 0.8f, HandType.RIGHT), // C5
                        MidiNoteEvent(71, 5700, 400, 0.8f, HandType.RIGHT), // B4
                        MidiNoteEvent(69, 6200, 600, 0.8f, HandType.RIGHT), // A4
                        MidiNoteEvent(67, 6900, 900, 0.8f, HandType.RIGHT)  // G4
                    )
                )
            )
        ),

        // 10. The Entertainer - Premium
        Song(
            id = "the_entertainer",
            title = "The Entertainer",
            composer = "Scott Joplin",
            difficulty = "Hard",
            bpm = 135,
            timeSignature = "2/4",
            isPremium = true,
            tracks = listOf(
                SongTrack(
                    hand = HandType.RIGHT,
                    notes = listOf(
                        MidiNoteEvent(75, 0, 200, 0.8f, HandType.RIGHT),   // D#5
                        MidiNoteEvent(76, 220, 200, 0.8f, HandType.RIGHT),  // E5
                        MidiNoteEvent(72, 440, 200, 0.8f, HandType.RIGHT),  // C5
                        MidiNoteEvent(69, 660, 400, 0.8f, HandType.RIGHT),  // A4
                        MidiNoteEvent(71, 1100, 200, 0.8f, HandType.RIGHT), // B4
                        MidiNoteEvent(67, 1320, 400, 0.8f, HandType.RIGHT)  // G4
                    )
                )
            )
        )
    )

    fun getById(id: String): Song? = ALL_SONGS.find { it.id == id }
}
