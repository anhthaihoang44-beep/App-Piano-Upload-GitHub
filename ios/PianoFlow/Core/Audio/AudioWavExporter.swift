import Foundation

public struct RecordedNoteEvent: Codable, Hashable, Sendable {
    public let midiNumber: Int
    public let startMs: Int64
    public let durationMs: Int64
    public let velocity: Float

    public init(
        midiNumber: Int,
        startMs: Int64,
        durationMs: Int64 = 500,
        velocity: Float = 0.8
    ) {
        self.midiNumber = midiNumber
        self.startMs = startMs
        self.durationMs = durationMs
        self.velocity = velocity
    }

    private enum CodingKeys: String, CodingKey {
        case midiNumber = "midi"
        case startMs
        case durationMs
        case velocity
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        midiNumber = try container.decode(Int.self, forKey: .midiNumber)
        startMs = try container.decode(Int64.self, forKey: .startMs)
        durationMs = try container.decodeIfPresent(Int64.self, forKey: .durationMs) ?? 500
        velocity = try container.decodeIfPresent(Float.self, forKey: .velocity) ?? 0.8
    }
}

/// High-performance offline WAV audio renderer.
/// Converts a list of timestamped note events into an acoustic grand piano 16-bit 44.1kHz PCM WAV file.
public enum AudioWavExporter {

    public static let sampleRate: Int = 44_100

    public static func renderWavData(notes: [RecordedNoteEvent]) -> Data {
        guard !notes.isEmpty else {
            return makeWavHeader(pcmDataLength: 0, sampleRate: sampleRate, channels: 1, bitsPerSample: 16)
        }

        let maxEndMs = notes.map { $0.startMs + $0.durationMs }.max() ?? 0
        let totalDurationMs = maxEndMs + 1000 // 1s ring out
        let totalSamples = max(1, Int((Double(totalDurationMs) / 1000.0) * Double(sampleRate)))
        var mixBuffer = [Float](repeating: 0, count: totalSamples)

        for note in notes {
            let startSample = max(0, Int((Double(note.startMs) / 1000.0) * Double(sampleRate)))
            let noteDurationSamples = Int((Double(note.durationMs) / 1000.0) * Double(sampleRate))
            let noteSamples = max(noteDurationSamples, Int(Double(sampleRate) * 1.5))

            let freq = NotePitch.fromMidi(note.midiNumber)?.frequencyHz ?? 440.0
            let decay = 2.0 + Float(note.midiNumber - 36) * 0.04

            for i in 0..<noteSamples {
                let mixIndex = startSample + i
                if mixIndex >= totalSamples { break }

                let t = Float(i) / Float(sampleRate)
                let envelope = exp(-decay * t)

                var sample: Float = 0.65 * sin(2.0 * Float.pi * freq * t)
                sample += 0.25 * sin(2.0 * Float.pi * 2.0 * freq * t)
                sample += 0.15 * sin(2.0 * Float.pi * 3.0 * freq * t)
                sample += 0.08 * sin(2.0 * Float.pi * 4.0 * freq * t)

                mixBuffer[mixIndex] += sample * envelope * note.velocity * 0.35
            }
        }

        let pcmDataLength = totalSamples * 2
        var output = makeWavHeader(
            pcmDataLength: pcmDataLength,
            sampleRate: sampleRate,
            channels: 1,
            bitsPerSample: 16
        )
        output.reserveCapacity(44 + pcmDataLength)

        for sample in mixBuffer {
            let clamped = min(max(sample, -1.0), 1.0)
            let pcmValue = Int16(clamped * 32767.0)
            var le = pcmValue.littleEndian
            withUnsafeBytes(of: &le) { output.append(contentsOf: $0) }
        }

        return output
    }

    public static func exportToWav(fileName: String, notes: [RecordedNoteEvent]) async throws -> URL {
        try await Task.detached(priority: .userInitiated) {
            let cacheRoot = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first
                ?? FileManager.default.temporaryDirectory
            let audioDir = cacheRoot.appendingPathComponent("audio", isDirectory: true)
            try FileManager.default.createDirectory(at: audioDir, withIntermediateDirectories: true)

            let allowed = CharacterSet.alphanumerics.union(CharacterSet(charactersIn: "_-"))
            let safeName = fileName.unicodeScalars.map { allowed.contains($0) ? Character($0) : "_" }
            let cleanName = String(safeName).isEmpty ? "PianoFlow_Recording" : String(safeName)
            let outputURL = audioDir.appendingPathComponent("\(cleanName).wav")

            let wavData = renderWavData(notes: notes)
            try wavData.write(to: outputURL, options: .atomic)
            return outputURL
        }.value
    }

    public static func makeWavHeader(
        pcmDataLength: Int,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) -> Data {
        var header = Data()
        header.reserveCapacity(44)

        let totalDataLen = UInt32(pcmDataLength + 36)
        let byteRate = UInt32(sampleRate * channels * bitsPerSample / 8)
        let blockAlign = UInt16(channels * bitsPerSample / 8)

        header.append(contentsOf: "RIFF".utf8)
        appendLE(totalDataLen, to: &header)
        header.append(contentsOf: "WAVE".utf8)
        header.append(contentsOf: "fmt ".utf8)
        appendLE(UInt32(16), to: &header) // Subchunk1Size for PCM
        appendLE(UInt16(1), to: &header)  // AudioFormat 1 = PCM
        appendLE(UInt16(channels), to: &header)
        appendLE(UInt32(sampleRate), to: &header)
        appendLE(byteRate, to: &header)
        appendLE(blockAlign, to: &header)
        appendLE(UInt16(bitsPerSample), to: &header)
        header.append(contentsOf: "data".utf8)
        appendLE(UInt32(pcmDataLength), to: &header)

        return header
    }

    private static func appendLE<T: FixedWidthInteger>(_ value: T, to data: inout Data) {
        var le = value.littleEndian
        withUnsafeBytes(of: &le) { data.append(contentsOf: $0) }
    }
}
