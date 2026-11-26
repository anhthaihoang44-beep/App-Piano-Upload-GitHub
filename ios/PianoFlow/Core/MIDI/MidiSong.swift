import Foundation

public enum HandType: String, Codable, CaseIterable, Hashable, Sendable {
    case right = "Right"
    case left = "Left"
    case both = "Both"

    public static func fromString(_ str: String) -> HandType {
        switch str.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() {
        case "left":
            return .left
        case "both":
            return .both
        default:
            return .right
        }
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        let raw = try container.decode(String.self)
        self = HandType.fromString(raw)
    }
}

public struct MidiNoteEvent: Codable, Hashable, Identifiable, Sendable {
    public let midi: Int
    public let startMs: Int64
    public let durationMs: Int64
    public let velocity: Float
    public let hand: HandType

    public var id: String {
        "\(midi)_\(startMs)_\(durationMs)_\(hand.rawValue)"
    }

    public init(
        midi: Int,
        startMs: Int64,
        durationMs: Int64 = 500,
        velocity: Float = 0.8,
        hand: HandType = .right
    ) {
        self.midi = midi
        self.startMs = startMs
        self.durationMs = durationMs
        self.velocity = velocity
        self.hand = hand
    }

    private enum CodingKeys: String, CodingKey {
        case midi
        case startMs
        case durationMs
        case velocity
        case hand
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        midi = try container.decode(Int.self, forKey: .midi)
        startMs = try container.decode(Int64.self, forKey: .startMs)
        durationMs = try container.decodeIfPresent(Int64.self, forKey: .durationMs) ?? 500
        velocity = try container.decodeIfPresent(Float.self, forKey: .velocity) ?? 0.8
        hand = try container.decodeIfPresent(HandType.self, forKey: .hand) ?? .right
    }
}

public struct SongTrack: Codable, Hashable, Sendable {
    public let hand: HandType
    public let notes: [MidiNoteEvent]

    public init(hand: HandType, notes: [MidiNoteEvent]) {
        self.hand = hand
        self.notes = notes
    }
}

public struct Song: Codable, Identifiable, Hashable, Sendable {
    public let id: String
    public let title: String
    public let composer: String
    public let difficulty: String
    public let bpm: Int
    public let timeSignature: String
    public let isPremium: Bool
    public let tracks: [SongTrack]

    public var allNotes: [MidiNoteEvent] {
        tracks.flatMap { track in
            track.notes.map {
                MidiNoteEvent(
                    midi: $0.midi,
                    startMs: $0.startMs,
                    durationMs: $0.durationMs,
                    velocity: $0.velocity,
                    hand: track.hand
                )
            }
        }
        .sorted { $0.startMs < $1.startMs }
    }

    public var durationMs: Int64 {
        allNotes.map { $0.startMs + $0.durationMs }.max() ?? 0
    }

    public init(
        id: String,
        title: String,
        composer: String = "Traditional",
        difficulty: String = "Easy",
        bpm: Int = 120,
        timeSignature: String = "4/4",
        isPremium: Bool = false,
        tracks: [SongTrack]
    ) {
        self.id = id.isEmpty
            ? title.lowercased().replacingOccurrences(of: " ", with: "_")
            : id
        self.title = title
        self.composer = composer
        self.difficulty = difficulty
        self.bpm = bpm
        self.timeSignature = timeSignature
        self.isPremium = isPremium
        self.tracks = tracks
    }

    private enum CodingKeys: String, CodingKey {
        case id
        case title
        case composer
        case difficulty
        case bpm
        case timeSignature
        case isPremium
        case tracks
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let title = try container.decode(String.self, forKey: .title)
        let rawId = try container.decodeIfPresent(String.self, forKey: .id) ?? ""
        self.id = rawId.isEmpty ? title.lowercased().replacingOccurrences(of: " ", with: "_") : rawId
        self.title = title
        self.composer = try container.decodeIfPresent(String.self, forKey: .composer) ?? "Traditional"
        self.difficulty = try container.decodeIfPresent(String.self, forKey: .difficulty) ?? "Easy"
        self.bpm = try container.decodeIfPresent(Int.self, forKey: .bpm) ?? 120
        self.timeSignature = try container.decodeIfPresent(String.self, forKey: .timeSignature) ?? "4/4"
        self.isPremium = try container.decodeIfPresent(Bool.self, forKey: .isPremium) ?? false
        let decodedTracks = try container.decodeIfPresent([SongTrack].self, forKey: .tracks) ?? []
        self.tracks = decodedTracks.map { track in
            SongTrack(
                hand: track.hand,
                notes: track.notes.map { note in
                    MidiNoteEvent(
                        midi: note.midi,
                        startMs: note.startMs,
                        durationMs: note.durationMs,
                        velocity: note.velocity,
                        hand: track.hand
                    )
                }
            )
        }
    }

    public static func fromJSON(_ jsonString: String) throws -> Song {
        guard let data = jsonString.data(using: .utf8) else {
            throw NSError(domain: "PianoFlow.Song", code: 1, userInfo: [NSLocalizedDescriptionKey: "Invalid UTF-8 JSON"])
        }
        return try JSONDecoder().decode(Song.self, from: data)
    }
}
