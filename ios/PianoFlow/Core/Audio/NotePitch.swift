import Foundation

public enum NotationType: String, Codable, CaseIterable, Sendable {
    case cde = "CDE"
    case doReMi = "DO_RE_MI"
    case off = "OFF"
}

public struct NotePitch: Identifiable, Hashable, Sendable {
    public let midiNumber: Int
    public let nameEn: String
    public let nameSolfege: String
    public let octave: Int
    public let isBlack: Bool
    public let frequencyHz: Float

    public var id: Int { midiNumber }

    private static let noteNamesEn = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"]
    private static let noteNamesSolfege = ["Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si"]
    private static let blackKeyIndices: Set<Int> = [1, 3, 6, 8, 10]

    public static let allNotes: [NotePitch] = (21...108).map { midi in
        let noteIndex = (midi - 12) % 12
        let octave = (midi - 12) / 12
        let isBlack = blackKeyIndices.contains(noteIndex)
        let freq = Float(440.0 * pow(2.0, Double(midi - 69) / 12.0))
        return NotePitch(
            midiNumber: midi,
            nameEn: "\(noteNamesEn[noteIndex])\(octave)",
            nameSolfege: "\(noteNamesSolfege[noteIndex])\(octave)",
            octave: octave,
            isBlack: isBlack,
            frequencyHz: freq
        )
    }

    private static let noteMap: [Int: NotePitch] = Dictionary(
        uniqueKeysWithValues: allNotes.map { ($0.midiNumber, $0) }
    )

    public static func fromMidi(_ midiNumber: Int) -> NotePitch? {
        noteMap[midiNumber]
    }

    public static func displayName(for midiNumber: Int, notation: NotationType) -> String {
        guard let note = fromMidi(midiNumber) else { return "" }
        switch notation {
        case .cde:
            return note.nameEn
        case .doReMi:
            return note.nameSolfege
        case .off:
            return ""
        }
    }
}
