import SwiftUI

public struct SongPlayerScreen: View {

    @StateObject private var viewModel: SongPlayerViewModel
    let onBack: () -> Void

    @State private var showSpeedSheet = false

    private let startMidi = 48 // C3
    private let endMidi = 84   // C6

    public init(
        songId: String,
        audioEngine: AudioEngineProtocol,
        dataStore: PianoFlowDataStore,
        onBack: @escaping () -> Void
    ) {
        _viewModel = StateObject(
            wrappedValue: SongPlayerViewModel(
                songId: songId,
                audioEngine: audioEngine,
                dataStore: dataStore
            )
        )
        self.onBack = onBack
    }

    public var body: some View {
        VStack(spacing: 0) {
            topToolbar
                .padding(.horizontal, 8)
                .padding(.vertical, 4)

            if viewModel.playMode == .perform {
                performHud
                    .padding(.horizontal, 16)
                    .padding(.vertical, 4)
            }

            FallingNotesView(
                song: viewModel.song,
                currentPositionMs: viewModel.currentPositionMs,
                startMidi: startMidi,
                endMidi: endMidi,
                keyWidthPt: 50
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            PianoCanvasView(
                startMidi: startMidi,
                endMidi: endMidi,
                keyWidthPt: 50,
                notationType: .cde,
                highlightedKeys: viewModel.highlightedKeys,
                onKeyDown: { viewModel.onUserKeyDown($0) },
                onKeyUp: { viewModel.onUserKeyUp($0) }
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            bottomControls
                .padding(8)
        }
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
        .onDisappear {
            viewModel.stopLoop()
        }
        .sheet(isPresented: $showSpeedSheet) {
            speedSheet
                .presentationDetents([.height(180)])
                .presentationDragIndicator(.visible)
        }
        .sheet(item: Binding(
            get: { viewModel.scoreSummary.map { IdentifiableScoreSummary(summary: $0) } },
            set: { if $0 == nil { viewModel.dismissSummary() } }
        )) { item in
            scoreSummarySheet(item.summary)
                .presentationDetents([.medium])
        }
    }

    private var topToolbar: some View {
        HStack(spacing: 8) {
            Button {
                viewModel.stopLoop()
                onBack()
            } label: {
                Image(systemName: "chevron.left")
                    .font(.title3.bold())
                    .foregroundColor(.white)
                    .padding(6)
            }

            VStack(alignment: .leading, spacing: 1) {
                Text(viewModel.song.title)
                    .font(.subheadline.bold())
                    .foregroundColor(.white)
                    .lineLimit(1)
                Text(viewModel.song.composer)
                    .font(.caption2)
                    .foregroundColor(.gray)
                    .lineLimit(1)
            }

            Spacer()

            HStack(spacing: 4) {
                ForEach(PlayMode.allCases, id: \.self) { mode in
                    let isSelected = viewModel.playMode == mode
                    Button {
                        viewModel.setMode(mode)
                    } label: {
                        Text(modeLabel(mode))
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(isSelected ? .white : .gray)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 6)
                            .background(isSelected ? PianoFlowColors.brandPrimary : PianoFlowColors.surfaceElevatedDark)
                            .clipShape(Capsule())
                    }
                }
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 6)
        .background(PianoFlowColors.surfaceDark)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private var performHud: some View {
        HStack {
            Text(L10n.format("song_score_value", viewModel.score))
                .font(.subheadline.bold())
                .foregroundColor(PianoFlowColors.brandSecondary)

            Spacer()

            if viewModel.currentCombo > 1 {
                Text(L10n.format("song_combo_value", viewModel.currentCombo))
                    .font(.subheadline.weight(.heavy))
                    .foregroundColor(PianoFlowColors.brandPrimary)
            }

            Spacer()

            if let rating = viewModel.lastHitRating {
                let ratingText: String = {
                    switch rating {
                    case .perfect: return L10n.tr("song_hit_perfect")
                    case .good: return L10n.tr("song_hit_good")
                    case .miss: return L10n.tr("song_hit_miss")
                    }
                }()
                Text(ratingText)
                    .font(.caption.bold())
                    .foregroundColor(rating == .perfect ? PianoFlowColors.goldStarColor : PianoFlowColors.brandSecondary)
            }
        }
    }

    private var bottomControls: some View {
        let maxDuration = max(1000.0, Double(viewModel.song.durationMs))

        return VStack(spacing: 8) {
            Slider(
                value: Binding(
                    get: { min(max(0, Double(viewModel.currentPositionMs)), maxDuration) },
                    set: { viewModel.seekTo(Int64($0)) }
                ),
                in: 0...maxDuration
            )
            .tint(PianoFlowColors.brandPrimary)

            HStack {
                HStack(spacing: 4) {
                    handChip(hand: .both, title: L10n.tr("song_hand_both"), color: .white)
                    handChip(hand: .right, title: L10n.tr("song_hand_right"), color: PianoFlowColors.rightHandColor)
                    handChip(hand: .left, title: L10n.tr("song_hand_left"), color: PianoFlowColors.leftHandColor)
                }

                Spacer()

                Button {
                    viewModel.togglePlayPause()
                } label: {
                    Image(systemName: viewModel.isPlaying ? "pause.fill" : "play.fill")
                        .font(.title3)
                        .foregroundColor(.white)
                        .frame(width: 44, height: 44)
                        .background(PianoFlowColors.brandPrimary)
                        .clipShape(Circle())
                }

                Spacer()

                HStack(spacing: 10) {
                    Button {
                        showSpeedSheet = true
                    } label: {
                        Text(String(format: "%.2gx", viewModel.playbackSpeed))
                            .font(.caption.bold())
                            .foregroundColor(PianoFlowColors.brandSecondary)
                    }

                    Button {
                        viewModel.toggleLoop()
                    } label: {
                        Image(systemName: "repeat")
                            .foregroundColor(viewModel.isLooping ? PianoFlowColors.brandSecondary : .gray)
                    }
                }
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(PianoFlowColors.surfaceDark)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private func handChip(hand: HandType, title: String, color: Color) -> some View {
        let selected = viewModel.selectedHand == hand
        return Button {
            viewModel.setSelectedHand(hand)
        } label: {
            Text(title)
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(color)
                .padding(.horizontal, 8)
                .padding(.vertical, 5)
                .background(selected ? PianoFlowColors.brandPrimary.opacity(0.35) : PianoFlowColors.surfaceElevatedDark)
                .clipShape(Capsule())
        }
    }

    private var speedSheet: some View {
        VStack(spacing: 16) {
            Text(L10n.tr("song_speed"))
                .font(.headline)
                .foregroundColor(.white)

            HStack(spacing: 10) {
                ForEach([Float(0.5), 0.75, 1.0, 1.25, 1.5], id: \.self) { speed in
                    let isSelected = viewModel.playbackSpeed == speed
                    Button {
                        viewModel.setSpeed(speed)
                        showSpeedSheet = false
                    } label: {
                        Text(String(format: "%.2gx", speed))
                            .font(.subheadline.bold())
                            .foregroundColor(isSelected ? PianoFlowColors.backgroundDark : .white)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .background(isSelected ? PianoFlowColors.brandSecondary : PianoFlowColors.surfaceElevatedDark)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                }
            }
        }
        .padding(24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(PianoFlowColors.surfaceDark)
    }

    private func scoreSummarySheet(_ summary: ScoreSummary) -> some View {
        VStack(spacing: 14) {
            Text(summary.stars >= 2 ? L10n.tr("song_great_job") : L10n.tr("song_keep_practicing"))
                .font(.title2.bold())
                .foregroundColor(.white)

            HStack(spacing: 8) {
                ForEach(1...3, id: \.self) { star in
                    Image(systemName: "star.fill")
                        .font(.largeTitle)
                        .foregroundColor(star <= summary.stars ? PianoFlowColors.goldStarColor : .gray)
                }
            }

            Text(L10n.format("song_score_value", summary.score))
                .font(.title3.bold())
                .foregroundColor(PianoFlowColors.brandSecondary)
            Text(L10n.format("song_accuracy_value", Int(summary.accuracyPercent)))
                .foregroundColor(.white)
            Text(L10n.format("song_max_combo_value", summary.maxCombo))
                .foregroundColor(.gray)
            Text(L10n.format("song_hits_breakdown", summary.perfectHits, summary.goodHits, summary.misses))
                .font(.caption)
                .foregroundColor(.gray)

            HStack(spacing: 12) {
                Button(L10n.tr("song_try_again")) {
                    viewModel.seekTo(0)
                    viewModel.dismissSummary()
                    viewModel.play()
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
                .background(PianoFlowColors.brandPrimary)
                .foregroundColor(.white)
                .clipShape(RoundedRectangle(cornerRadius: 10))

                Button(L10n.tr("common_done")) {
                    viewModel.dismissSummary()
                    onBack()
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
                .background(PianoFlowColors.surfaceElevatedDark)
                .foregroundColor(.white)
                .clipShape(RoundedRectangle(cornerRadius: 10))
            }
            .padding(.top, 8)
        }
        .padding(24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(PianoFlowColors.surfaceDark)
    }

    private func modeLabel(_ mode: PlayMode) -> String {
        switch mode {
        case .watch: return L10n.tr("song_mode_watch")
        case .follow: return L10n.tr("song_mode_follow")
        case .perform: return L10n.tr("song_mode_perform")
        }
    }
}

private struct IdentifiableScoreSummary: Identifiable {
    let id = UUID()
    let summary: ScoreSummary
}
