import Foundation

public enum BundledSongs {

    public static let allSongs: [Song] = [
        // 1. Ode to Joy (Beethoven) - Free
        Song(
            id: "ode_to_joy",
            title: "Ode to Joy",
            composer: "Ludwig van Beethoven",
            difficulty: "Easy",
            bpm: 110,
            timeSignature: "4/4",
            isPremium: false,
            tracks: [
                SongTrack(
                    hand: .right,
                    notes: [
                        MidiNoteEvent(midi: 64, startMs: 0, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 500, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 65, startMs: 1000, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 1500, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 2000, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 65, startMs: 2500, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 3000, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 62, startMs: 3500, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 60, startMs: 4000, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 60, startMs: 4500, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 62, startMs: 5000, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 5500, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 6000, durationMs: 650, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 62, startMs: 6750, durationMs: 250, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 62, startMs: 7000, durationMs: 900, velocity: 0.8, hand: .right)
                    ]
                ),
                SongTrack(
                    hand: .left,
                    notes: [
                        MidiNoteEvent(midi: 48, startMs: 0, durationMs: 1800, velocity: 0.6, hand: .left),
                        MidiNoteEvent(midi: 48, startMs: 2000, durationMs: 1800, velocity: 0.6, hand: .left),
                        MidiNoteEvent(midi: 48, startMs: 4000, durationMs: 1800, velocity: 0.6, hand: .left),
                        MidiNoteEvent(midi: 43, startMs: 6000, durationMs: 1800, velocity: 0.6, hand: .left)
                    ]
                )
            ]
        ),

        // 2. Twinkle Twinkle Little Star - Free
        Song(
            id: "twinkle_twinkle",
            title: "Twinkle Twinkle Little Star",
            composer: "Traditional",
            difficulty: "Easy",
            bpm: 100,
            timeSignature: "4/4",
            isPremium: false,
            tracks: [
                SongTrack(
                    hand: .right,
                    notes: [
                        MidiNoteEvent(midi: 60, startMs: 0, durationMs: 500, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 60, startMs: 600, durationMs: 500, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 1200, durationMs: 500, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 1800, durationMs: 500, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 69, startMs: 2400, durationMs: 500, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 69, startMs: 3000, durationMs: 500, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 3600, durationMs: 1000, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 65, startMs: 4800, durationMs: 500, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 65, startMs: 5400, durationMs: 500, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 6000, durationMs: 500, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 6600, durationMs: 500, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 62, startMs: 7200, durationMs: 500, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 62, startMs: 7800, durationMs: 500, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 60, startMs: 8400, durationMs: 1000, velocity: 0.8, hand: .right)
                    ]
                ),
                SongTrack(
                    hand: .left,
                    notes: [
                        MidiNoteEvent(midi: 48, startMs: 0, durationMs: 1000, velocity: 0.6, hand: .left),
                        MidiNoteEvent(midi: 52, startMs: 1200, durationMs: 1000, velocity: 0.6, hand: .left),
                        MidiNoteEvent(midi: 53, startMs: 2400, durationMs: 1000, velocity: 0.6, hand: .left),
                        MidiNoteEvent(midi: 52, startMs: 3600, durationMs: 1000, velocity: 0.6, hand: .left),
                        MidiNoteEvent(midi: 48, startMs: 4800, durationMs: 1000, velocity: 0.6, hand: .left),
                        MidiNoteEvent(midi: 47, startMs: 6000, durationMs: 1000, velocity: 0.6, hand: .left),
                        MidiNoteEvent(midi: 43, startMs: 7200, durationMs: 1000, velocity: 0.6, hand: .left),
                        MidiNoteEvent(midi: 48, startMs: 8400, durationMs: 1000, velocity: 0.6, hand: .left)
                    ]
                )
            ]
        ),

        // 3. Happy Birthday - Free
        Song(
            id: "happy_birthday",
            title: "Happy Birthday",
            composer: "Patty & Mildred Hill",
            difficulty: "Easy",
            bpm: 100,
            timeSignature: "3/4",
            isPremium: false,
            tracks: [
                SongTrack(
                    hand: .right,
                    notes: [
                        MidiNoteEvent(midi: 60, startMs: 0, durationMs: 350, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 60, startMs: 400, durationMs: 200, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 62, startMs: 650, durationMs: 600, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 60, startMs: 1300, durationMs: 600, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 65, startMs: 1950, durationMs: 600, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 2600, durationMs: 1200, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 60, startMs: 3900, durationMs: 350, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 60, startMs: 4300, durationMs: 200, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 62, startMs: 4550, durationMs: 600, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 60, startMs: 5200, durationMs: 600, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 5850, durationMs: 600, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 65, startMs: 6500, durationMs: 1200, velocity: 0.8, hand: .right)
                    ]
                )
            ]
        ),

        // 4. Jingle Bells - Free
        Song(
            id: "jingle_bells",
            title: "Jingle Bells",
            composer: "James Pierpont",
            difficulty: "Easy",
            bpm: 120,
            timeSignature: "4/4",
            isPremium: false,
            tracks: [
                SongTrack(
                    hand: .right,
                    notes: [
                        MidiNoteEvent(midi: 64, startMs: 0, durationMs: 400, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 500, durationMs: 400, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 1000, durationMs: 800, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 2000, durationMs: 400, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 2500, durationMs: 400, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 3000, durationMs: 800, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 4000, durationMs: 400, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 4500, durationMs: 400, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 60, startMs: 5000, durationMs: 600, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 62, startMs: 5700, durationMs: 250, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 6000, durationMs: 1200, velocity: 0.8, hand: .right)
                    ]
                )
            ]
        ),

        // 5. Für Elise - Premium
        Song(
            id: "fur_elise",
            title: "Für Elise",
            composer: "Ludwig van Beethoven",
            difficulty: "Intermediate",
            bpm: 130,
            timeSignature: "3/8",
            isPremium: true,
            tracks: [
                SongTrack(
                    hand: .right,
                    notes: [
                        MidiNoteEvent(midi: 76, startMs: 0, durationMs: 230, velocity: 0.85, hand: .right),
                        MidiNoteEvent(midi: 75, startMs: 250, durationMs: 230, velocity: 0.85, hand: .right),
                        MidiNoteEvent(midi: 76, startMs: 500, durationMs: 230, velocity: 0.85, hand: .right),
                        MidiNoteEvent(midi: 75, startMs: 750, durationMs: 230, velocity: 0.85, hand: .right),
                        MidiNoteEvent(midi: 76, startMs: 1000, durationMs: 230, velocity: 0.85, hand: .right),
                        MidiNoteEvent(midi: 71, startMs: 1250, durationMs: 230, velocity: 0.85, hand: .right),
                        MidiNoteEvent(midi: 74, startMs: 1500, durationMs: 230, velocity: 0.85, hand: .right),
                        MidiNoteEvent(midi: 72, startMs: 1750, durationMs: 230, velocity: 0.85, hand: .right),
                        MidiNoteEvent(midi: 69, startMs: 2000, durationMs: 600, velocity: 0.85, hand: .right),
                        MidiNoteEvent(midi: 60, startMs: 2750, durationMs: 230, velocity: 0.75, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 3000, durationMs: 230, velocity: 0.75, hand: .right),
                        MidiNoteEvent(midi: 69, startMs: 3250, durationMs: 230, velocity: 0.75, hand: .right),
                        MidiNoteEvent(midi: 71, startMs: 3500, durationMs: 600, velocity: 0.85, hand: .right)
                    ]
                ),
                SongTrack(
                    hand: .left,
                    notes: [
                        MidiNoteEvent(midi: 45, startMs: 2000, durationMs: 800, velocity: 0.65, hand: .left),
                        MidiNoteEvent(midi: 52, startMs: 2300, durationMs: 600, velocity: 0.65, hand: .left),
                        MidiNoteEvent(midi: 57, startMs: 2600, durationMs: 600, velocity: 0.65, hand: .left),
                        MidiNoteEvent(midi: 40, startMs: 3500, durationMs: 800, velocity: 0.65, hand: .left)
                    ]
                )
            ]
        ),

        // 6. Canon in D (Pachelbel) - Premium
        Song(
            id: "canon_in_d",
            title: "Canon in D",
            composer: "Johann Pachelbel",
            difficulty: "Intermediate",
            bpm: 75,
            timeSignature: "4/4",
            isPremium: true,
            tracks: [
                SongTrack(
                    hand: .right,
                    notes: [
                        MidiNoteEvent(midi: 74, startMs: 0, durationMs: 750, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 73, startMs: 800, durationMs: 750, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 71, startMs: 1600, durationMs: 750, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 69, startMs: 2400, durationMs: 750, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 3200, durationMs: 750, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 66, startMs: 4000, durationMs: 750, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 4800, durationMs: 750, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 69, startMs: 5600, durationMs: 750, velocity: 0.8, hand: .right)
                    ]
                ),
                SongTrack(
                    hand: .left,
                    notes: [
                        MidiNoteEvent(midi: 50, startMs: 0, durationMs: 750, velocity: 0.7, hand: .left),
                        MidiNoteEvent(midi: 45, startMs: 800, durationMs: 750, velocity: 0.7, hand: .left),
                        MidiNoteEvent(midi: 47, startMs: 1600, durationMs: 750, velocity: 0.7, hand: .left),
                        MidiNoteEvent(midi: 42, startMs: 2400, durationMs: 750, velocity: 0.7, hand: .left),
                        MidiNoteEvent(midi: 43, startMs: 3200, durationMs: 750, velocity: 0.7, hand: .left),
                        MidiNoteEvent(midi: 38, startMs: 4000, durationMs: 750, velocity: 0.7, hand: .left),
                        MidiNoteEvent(midi: 43, startMs: 4800, durationMs: 750, velocity: 0.7, hand: .left),
                        MidiNoteEvent(midi: 45, startMs: 5600, durationMs: 750, velocity: 0.7, hand: .left)
                    ]
                )
            ]
        ),

        // 7. Greensleeves - Premium
        Song(
            id: "greensleeves",
            title: "Greensleeves",
            composer: "Traditional English",
            difficulty: "Medium",
            bpm: 96,
            timeSignature: "6/8",
            isPremium: true,
            tracks: [
                SongTrack(
                    hand: .right,
                    notes: [
                        MidiNoteEvent(midi: 69, startMs: 0, durationMs: 550, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 72, startMs: 625, durationMs: 900, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 74, startMs: 1600, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 76, startMs: 2100, durationMs: 700, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 77, startMs: 2850, durationMs: 300, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 76, startMs: 3200, durationMs: 700, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 74, startMs: 4000, durationMs: 900, velocity: 0.8, hand: .right)
                    ]
                )
            ]
        ),

        // 8. Minuet in G (Bach) - Premium
        Song(
            id: "minuet_in_g",
            title: "Minuet in G Major",
            composer: "Christian Petzold / J.S. Bach",
            difficulty: "Intermediate",
            bpm: 120,
            timeSignature: "3/4",
            isPremium: true,
            tracks: [
                SongTrack(
                    hand: .right,
                    notes: [
                        MidiNoteEvent(midi: 74, startMs: 0, durationMs: 480, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 500, durationMs: 240, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 69, startMs: 750, durationMs: 240, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 71, startMs: 1000, durationMs: 240, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 72, startMs: 1250, durationMs: 240, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 74, startMs: 1500, durationMs: 480, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 2000, durationMs: 480, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 2500, durationMs: 480, velocity: 0.8, hand: .right)
                    ]
                )
            ]
        ),

        // 9. Brahms' Lullaby - Premium
        Song(
            id: "brahms_lullaby",
            title: "Brahms' Lullaby",
            composer: "Johannes Brahms",
            difficulty: "Easy",
            bpm: 85,
            timeSignature: "3/4",
            isPremium: true,
            tracks: [
                SongTrack(
                    hand: .right,
                    notes: [
                        MidiNoteEvent(midi: 64, startMs: 0, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 500, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 1000, durationMs: 900, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 2000, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 2500, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 3000, durationMs: 900, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 64, startMs: 4000, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 4500, durationMs: 450, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 72, startMs: 5000, durationMs: 600, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 71, startMs: 5700, durationMs: 400, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 69, startMs: 6200, durationMs: 600, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 6900, durationMs: 900, velocity: 0.8, hand: .right)
                    ]
                )
            ]
        ),

        // 10. The Entertainer - Premium
        Song(
            id: "the_entertainer",
            title: "The Entertainer",
            composer: "Scott Joplin",
            difficulty: "Hard",
            bpm: 135,
            timeSignature: "2/4",
            isPremium: true,
            tracks: [
                SongTrack(
                    hand: .right,
                    notes: [
                        MidiNoteEvent(midi: 75, startMs: 0, durationMs: 200, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 76, startMs: 220, durationMs: 200, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 72, startMs: 440, durationMs: 200, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 69, startMs: 660, durationMs: 400, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 71, startMs: 1100, durationMs: 200, velocity: 0.8, hand: .right),
                        MidiNoteEvent(midi: 67, startMs: 1320, durationMs: 400, velocity: 0.8, hand: .right)
                    ]
                )
            ]
        )
    ]

    public static func getById(_ id: String) -> Song? {
        allSongs.first { $0.id == id }
    }
}
