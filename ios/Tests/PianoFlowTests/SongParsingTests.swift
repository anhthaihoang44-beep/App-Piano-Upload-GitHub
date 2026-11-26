import XCTest
@testable import PianoFlow

final class SongParsingTests: XCTestCase {

    func testNotePitchMapping() {
        let middleC = NotePitch.fromMidi(60)
        XCTAssertNotNil(middleC)
        XCTAssertEqual(middleC?.nameEn, "C4")
        XCTAssertEqual(middleC?.nameSolfege, "Do4")
        XCTAssertEqual(middleC?.isBlack, false)
        XCTAssertEqual(middleC?.octave, 4)

        let cSharp = NotePitch.fromMidi(61)
        XCTAssertNotNil(cSharp)
        XCTAssertEqual(cSharp?.nameEn, "C#4")
        XCTAssertEqual(cSharp?.isBlack, true)

        let a4 = NotePitch.fromMidi(69)
        XCTAssertNotNil(a4)
        XCTAssertEqual(a4?.frequencyHz ?? 0, 440, accuracy: 0.5)

        XCTAssertEqual(NotePitch.displayName(for: 60, notation: .cde), "C4")
        XCTAssertEqual(NotePitch.displayName(for: 60, notation: .doReMi), "Do4")
        XCTAssertEqual(NotePitch.displayName(for: 60, notation: .off), "")
    }

    func testSongFromJson() throws {
        let json = """
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
        """

        let song = try Song.fromJSON(json)
        XCTAssertEqual(song.id, "test_song")
        XCTAssertEqual(song.title, "Test Melody")
        XCTAssertEqual(song.bpm, 120)
        XCTAssertEqual(song.tracks.count, 2)

        let all = song.allNotes
        XCTAssertEqual(all.count, 3)
        XCTAssertEqual(song.durationMs, 1000)
        XCTAssertEqual(song.tracks[0].hand, .right)
        XCTAssertEqual(song.tracks[1].hand, .left)
    }

    func testBundledSongsIntegrity() {
        XCTAssertEqual(BundledSongs.allSongs.count, 10)
        for song in BundledSongs.allSongs {
            XCTAssertFalse(song.title.isEmpty)
            XCTAssertGreaterThan(song.bpm, 0)
            XCTAssertFalse(song.allNotes.isEmpty)
            XCTAssertGreaterThan(song.durationMs, 0)
        }
    }

    func testWavExporterHeaderAndSamples() {
        let data = AudioWavExporter.renderWavData(notes: [
            RecordedNoteEvent(midiNumber: 60, startMs: 0, durationMs: 300)
        ])
        XCTAssertGreaterThan(data.count, 44)
        XCTAssertEqual(String(decoding: data[0..<4], as: UTF8.self), "RIFF")
        XCTAssertEqual(String(decoding: data[8..<12], as: UTF8.self), "WAVE")
    }
}
