import SwiftUI
import UIKit

public struct RecordingListScreen: View {

    @ObservedObject var dataStore: PianoFlowDataStore
    let onReplayRecording: (RecordingEntity) -> Void

    @State private var exportingId: String?
    @State private var shareItem: SharePayload?

    public init(
        dataStore: PianoFlowDataStore,
        onReplayRecording: @escaping (RecordingEntity) -> Void
    ) {
        self.dataStore = dataStore
        self.onReplayRecording = onReplayRecording
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text(L10n.tr("recordings_title"))
                    .font(.title.bold())
                    .foregroundColor(.white)
                Text("Replay and share your piano performances")
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }
            .padding(.top, 12)

            if dataStore.recordings.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "waveform")
                        .font(.system(size: 56))
                        .foregroundColor(.gray.opacity(0.5))
                    Text(L10n.tr("recordings_empty"))
                        .font(.subheadline)
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }
                .frame(maxWidth: .infinity)
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 10) {
                        ForEach(dataStore.recordings, id: \.recordingId) { recording in
                            RecordingCardItem(
                                recording: recording,
                                isExporting: exportingId == recording.recordingId,
                                onPlay: { onReplayRecording(recording) },
                                onShare: { shareRecordingWav(recording) },
                                onDelete: { dataStore.deleteRecording(recording) }
                            )
                        }
                    }
                    .padding(.bottom, 24)
                }
            }
        }
        .padding(.horizontal, 16)
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
        .sheet(item: $shareItem) { payload in
            ShareSheetView(activityItems: [
                payload.url,
                "Listen to my piano performance on PianoFlow! 🎹"
            ])
        }
    }

    private func shareRecordingWav(_ recording: RecordingEntity) {
        Task {
            exportingId = recording.recordingId
            let notes = parseNotes(from: recording.notesJson)
            if let url = try? await AudioWavExporter.exportToWav(fileName: recording.title, notes: notes) {
                shareItem = SharePayload(url: url)
            }
            exportingId = nil
        }
    }

    private func parseNotes(from json: String) -> [RecordedNoteEvent] {
        guard let data = json.data(using: .utf8),
              let decoded = try? JSONDecoder().decode([RecordedNoteEvent].self, from: data) else {
            return []
        }
        return decoded
    }
}

private struct RecordingCardItem: View {
    let recording: RecordingEntity
    let isExporting: Bool
    let onPlay: () -> Void
    let onShare: () -> Void
    let onDelete: () -> Void

    private static let formatter: DateFormatter = {
        let df = DateFormatter()
        df.dateFormat = "MMM dd, yyyy • HH:mm"
        return df
    }()

    var body: some View {
        let date = Date(timeIntervalSince1970: TimeInterval(recording.createdAtMs) / 1000.0)
        let formattedDate = Self.formatter.string(from: date)
        let durationSec = max(0, recording.durationMs / 1000)
        let durationFormatted = String(format: "%02d:%02d", durationSec / 60, durationSec % 60)

        HStack(spacing: 14) {
            Button(action: onPlay) {
                ZStack {
                    Circle()
                        .fill(PianoFlowColors.brandPrimary.opacity(0.2))
                        .frame(width: 44, height: 44)
                    Image(systemName: "play.fill")
                        .foregroundColor(PianoFlowColors.brandSecondary)
                }
            }

            VStack(alignment: .leading, spacing: 3) {
                Text(recording.title)
                    .font(.headline)
                    .foregroundColor(.white)
                    .lineLimit(1)
                Text("\(formattedDate)  |  \(durationFormatted)  |  \(recording.noteCount) notes")
                    .font(.caption2)
                    .foregroundColor(.gray)
            }

            Spacer()

            if isExporting {
                ProgressView()
                    .tint(PianoFlowColors.brandSecondary)
            } else {
                Button(action: onShare) {
                    Image(systemName: "square.and.arrow.up")
                        .foregroundColor(PianoFlowColors.brandSecondary)
                }
            }

            Button(action: onDelete) {
                Image(systemName: "trash")
                    .foregroundColor(.gray)
            }
        }
        .padding(14)
        .background(PianoFlowColors.surfaceDark)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

private struct SharePayload: Identifiable {
    let id = UUID()
    let url: URL
}

private struct ShareSheetView: UIViewControllerRepresentable {
    let activityItems: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
