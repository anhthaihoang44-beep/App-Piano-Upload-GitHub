import Foundation

public enum MidiParserError: Error, LocalizedError {
    case invalidHeader(String)
    case truncatedData

    public var errorDescription: String? {
        switch self {
        case .invalidHeader(let found):
            return "Not a valid MIDI file: expected MThd, got \(found)"
        case .truncatedData:
            return "Unexpected end of MIDI file"
        }
    }
}

/// Lightweight native Standard MIDI File (SMF 0/1) parser.
/// Reads tracks, handles variable-length quantities, tempo meta events,
/// and extracts timed Note-On / Note-Off pairs into Song tracks.
public enum MidiParser {

    public static func parse(data: Data, title: String = "Imported Song") throws -> Song {
        var reader = ByteReader(data: data)

        let headerTag = try reader.readASCII(count: 4)
        guard headerTag == "MThd" else {
            throw MidiParserError.invalidHeader(headerTag)
        }

        let headerLength = Int(try reader.readUInt32BE())
        _ = try reader.readUInt16BE() // format
        let numTracks = Int(try reader.readUInt16BE())
        let division = Int(Int16(bitPattern: try reader.readUInt16BE()))

        if headerLength > 6 {
            try reader.skip(headerLength - 6)
        }

        var tracks: [SongTrack] = []
        var tempoMicrosPerQuarter: Int64 = 500_000 // 120 BPM default

        for _ in 0..<numTracks {
            guard reader.remaining >= 8 else { break }
            let trackTag = try reader.readASCII(count: 4)
            guard trackTag == "MTrk" else { break }

            let trackLen = Int(try reader.readUInt32BE())
            let trackEndPos = min(reader.offset + trackLen, data.count)

            var currentTick: Int64 = 0
            var runningStatus: UInt8 = 0
            var activeNotes: [Int: (startTick: Int64, velocity: Float)] = [:]
            var parsedNotes: [MidiNoteEvent] = []

            while reader.offset < trackEndPos {
                let deltaTick = try reader.readVarLen()
                currentTick += deltaTick

                var status = try reader.readUInt8()
                if status < 0x80 {
                    reader.offset -= 1
                    status = runningStatus
                } else {
                    runningStatus = status
                }

                if status == 0xFF {
                    let type = try reader.readUInt8()
                    let len = Int(try reader.readVarLen())
                    if type == 0x51 && len == 3 {
                        let b0 = Int64(try reader.readUInt8())
                        let b1 = Int64(try reader.readUInt8())
                        let b2 = Int64(try reader.readUInt8())
                        tempoMicrosPerQuarter = (b0 << 16) | (b1 << 8) | b2
                    } else {
                        try reader.skip(len)
                    }
                } else if (0xF0...0xF7).contains(status) {
                    let len = Int(try reader.readVarLen())
                    try reader.skip(len)
                } else {
                    let eventType = status & 0xF0
                    switch eventType {
                    case 0x90:
                        let note = Int(try reader.readUInt8())
                        let velocity = Int(try reader.readUInt8())
                        if velocity > 0 {
                            activeNotes[note] = (currentTick, Float(velocity) / 127.0)
                        } else if let started = activeNotes.removeValue(forKey: note) {
                            let startMs = ticksToMs(started.startTick, division: division, tempoMicrosPerQuarter: tempoMicrosPerQuarter)
                            let durationMs = max(80, ticksToMs(currentTick - started.startTick, division: division, tempoMicrosPerQuarter: tempoMicrosPerQuarter))
                            parsedNotes.append(MidiNoteEvent(midi: note, startMs: startMs, durationMs: durationMs, velocity: started.velocity, hand: .right))
                        }
                    case 0x80:
                        let note = Int(try reader.readUInt8())
                        _ = try reader.readUInt8() // release velocity
                        if let started = activeNotes.removeValue(forKey: note) {
                            let startMs = ticksToMs(started.startTick, division: division, tempoMicrosPerQuarter: tempoMicrosPerQuarter)
                            let durationMs = max(80, ticksToMs(currentTick - started.startTick, division: division, tempoMicrosPerQuarter: tempoMicrosPerQuarter))
                            parsedNotes.append(MidiNoteEvent(midi: note, startMs: startMs, durationMs: durationMs, velocity: started.velocity, hand: .right))
                        }
                    case 0xA0, 0xB0, 0xE0:
                        try reader.skip(2)
                    case 0xC0, 0xD0:
                        try reader.skip(1)
                    default:
                        break
                    }
                }
            }

            reader.offset = trackEndPos

            if !parsedNotes.isEmpty {
                let hand: HandType = tracks.isEmpty ? .right : .left
                tracks.append(SongTrack(hand: hand, notes: parsedNotes.sorted { $0.startMs < $1.startMs }))
            }
        }

        let bpm = min(max(Int(60_000_000 / max(1, tempoMicrosPerQuarter)), 30), 300)
        return Song(
            id: title.lowercased().replacingOccurrences(of: " ", with: "_"),
            title: title,
            composer: "Standard MIDI",
            difficulty: "Medium",
            bpm: bpm,
            timeSignature: "4/4",
            isPremium: false,
            tracks: tracks.isEmpty ? [SongTrack(hand: .right, notes: [])] : tracks
        )
    }

    public static func parse(url: URL, title: String = "Imported Song") throws -> Song {
        let data = try Data(contentsOf: url)
        return try parse(data: data, title: title)
    }

    private static func ticksToMs(_ ticks: Int64, division: Int, tempoMicrosPerQuarter: Int64) -> Int64 {
        guard division > 0 else { return ticks * 2 }
        return (ticks * tempoMicrosPerQuarter) / (Int64(division) * 1000)
    }

    private struct ByteReader {
        let data: Data
        var offset: Int = 0

        var remaining: Int {
            max(0, data.count - offset)
        }

        mutating func readUInt8() throws -> UInt8 {
            guard offset < data.count else { throw MidiParserError.truncatedData }
            let byte = data[data.index(data.startIndex, offsetBy: offset)]
            offset += 1
            return byte
        }

        mutating func readUInt16BE() throws -> UInt16 {
            let b0 = UInt16(try readUInt8())
            let b1 = UInt16(try readUInt8())
            return (b0 << 8) | b1
        }

        mutating func readUInt32BE() throws -> UInt32 {
            let b0 = UInt32(try readUInt8())
            let b1 = UInt32(try readUInt8())
            let b2 = UInt32(try readUInt8())
            let b3 = UInt32(try readUInt8())
            return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3
        }

        mutating func readASCII(count: Int) throws -> String {
            guard remaining >= count else { throw MidiParserError.truncatedData }
            let start = data.index(data.startIndex, offsetBy: offset)
            let end = data.index(start, offsetBy: count)
            offset += count
            return String(decoding: data[start..<end], as: UTF8.self)
        }

        mutating func skip(_ count: Int) throws {
            guard count >= 0, remaining >= count else { throw MidiParserError.truncatedData }
            offset += count
        }

        mutating func readVarLen() throws -> Int64 {
            var value: Int64 = 0
            var byte: UInt8
            repeat {
                byte = try readUInt8()
                value = (value << 7) | Int64(byte & 0x7F)
            } while (byte & 0x80) != 0
            return value
        }
    }
}
