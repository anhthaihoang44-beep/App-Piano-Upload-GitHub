import SwiftUI

public struct KeyboardScreen: View {

    @ObservedObject var viewModel: KeyboardViewModel
    let onNavigateToDoubleKeyboard: () -> Void
    let onNavigateToTwoPlayer: () -> Void

    @State private var showVolumeSheet = false
    @State private var showSaveRecordingAlert = false
    @State private var recordingSaveDurationMs: Int64 = 0
    @State private var recordingNoteCount: Int = 0
    @State private var recordingTitleInput: String = ""
    @State private var pulseActive = false
    @State private var dragStartOffset: CGFloat?

    private var startMidi: Int {
        min(max(36 + viewModel.currentOctaveOffset * 12, 21), 72)
    }

    private var endMidi: Int {
        min(max(startMidi + 36, 48), 108)
    }

    public init(
        viewModel: KeyboardViewModel,
        onNavigateToDoubleKeyboard: @escaping () -> Void,
        onNavigateToTwoPlayer: @escaping () -> Void
    ) {
        self.viewModel = viewModel
        self.onNavigateToDoubleKeyboard = onNavigateToDoubleKeyboard
        self.onNavigateToTwoPlayer = onNavigateToTwoPlayer
    }

    public var body: some View {
        VStack(spacing: 0) {
            toolbarCard
                .padding(.horizontal, 8)
                .padding(.vertical, 6)

            if !viewModel.isAudioLoaded {
                audioLoadingBanner
            }

            miniOctaveNavigator
                .padding(.horizontal, 8)

            Spacer().frame(height: 6)

            PianoCanvasView(
                startMidi: startMidi,
                endMidi: endMidi,
                scrollOffset: viewModel.scrollOffset,
                keyWidthPt: viewModel.keyWidthPt,
                notationType: viewModel.notationType,
                onKeyDown: { viewModel.onKeyDown($0) },
                onKeyUp: { viewModel.onKeyUp($0) }
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
        .sheet(isPresented: $showVolumeSheet) {
            volumeDialogSheet
                .presentationDetents([.height(220)])
                .presentationDragIndicator(.visible)
        }
        .alert(L10n.tr("recordings_save_dialog_title"), isPresented: $showSaveRecordingAlert) {
            TextField(L10n.tr("recordings_name_hint"), text: $recordingTitleInput)
            Button(L10n.tr("recordings_save")) {
                viewModel.saveRecording(title: recordingTitleInput, durationMs: recordingSaveDurationMs)
            }
            Button(L10n.tr("recordings_discard"), role: .cancel) {}
        } message: {
            Text("\(recordingNoteCount) notes recorded (\(Int(recordingSaveDurationMs / 1000))s)")
        }
    }

    private var toolbarCard: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                Button {
                    viewModel.toggleSustain()
                } label: {
                    Text(L10n.tr("keyboard_sustain"))
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(viewModel.isSustainEnabled ? PianoFlowColors.backgroundDark : .white)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 7)
                        .background(viewModel.isSustainEnabled ? PianoFlowColors.brandSecondary : PianoFlowColors.surfaceElevatedDark)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }

                Button {
                    viewModel.cycleNotationType()
                } label: {
                    Text(notationTitle)
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(PianoFlowColors.brandPrimary)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 7)
                        .background(PianoFlowColors.surfaceElevatedDark)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }

                HStack(spacing: 4) {
                    Button("◀ 8va") {
                        viewModel.shiftOctave(-1)
                    }
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(viewModel.currentOctaveOffset > -2 ? .white : .gray)
                    .disabled(viewModel.currentOctaveOffset <= -2)

                    Text("C\(startMidi / 12)")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(PianoFlowColors.brandSecondary)
                        .padding(.horizontal, 4)

                    Button("8va ▶") {
                        viewModel.shiftOctave(1)
                    }
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(viewModel.currentOctaveOffset < 2 ? .white : .gray)
                    .disabled(viewModel.currentOctaveOffset >= 2)
                }
                .padding(.horizontal, 4)

                HStack(spacing: 4) {
                    Button {
                        viewModel.zoomOut()
                    } label: {
                        Image(systemName: "minus.magnifyingglass")
                            .foregroundColor(.white)
                            .frame(width: 30, height: 30)
                    }

                    Button {
                        viewModel.zoomIn()
                    } label: {
                        Image(systemName: "plus.magnifyingglass")
                            .foregroundColor(.white)
                            .frame(width: 30, height: 30)
                    }
                }

                HStack(spacing: 6) {
                    Button {
                        showVolumeSheet = true
                    } label: {
                        Image(systemName: "speaker.wave.2.fill")
                            .foregroundColor(.white)
                            .frame(width: 30, height: 30)
                    }

                    Button(action: onNavigateToDoubleKeyboard) {
                        Image(systemName: "pianokeys")
                            .foregroundColor(PianoFlowColors.brandPrimary)
                            .frame(width: 30, height: 30)
                    }

                    Button(action: onNavigateToTwoPlayer) {
                        Image(systemName: "person.2.fill")
                            .foregroundColor(PianoFlowColors.brandSecondary)
                            .frame(width: 30, height: 30)
                    }

                    Button {
                        if !viewModel.isRecording {
                            viewModel.startRecording()
                            withAnimation(.easeInOut(duration: 0.6).repeatForever(autoreverses: true)) {
                                pulseActive = true
                            }
                        } else {
                            pulseActive = false
                            let result = viewModel.stopRecording()
                            recordingNoteCount = result.noteCount
                            recordingSaveDurationMs = result.durationMs
                            recordingTitleInput = ""
                            showSaveRecordingAlert = true
                        }
                    } label: {
                        Image(systemName: viewModel.isRecording ? "stop.circle.fill" : "record.circle")
                            .font(.system(size: 20))
                            .foregroundColor(PianoFlowColors.brandTertiary)
                            .opacity(viewModel.isRecording ? (pulseActive ? 0.4 : 1.0) : 1.0)
                            .frame(width: 32, height: 32)
                    }
                }
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
        }
        .background(PianoFlowColors.surfaceDark)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private var audioLoadingBanner: some View {
        VStack(spacing: 4) {
            Text(L10n.tr("keyboard_loading_audio"))
                .font(.caption2)
                .foregroundColor(PianoFlowColors.brandSecondary)
            ProgressView(value: Double(viewModel.audioLoadingProgress))
                .tint(PianoFlowColors.brandSecondary)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 4)
    }

    private var miniOctaveNavigator: some View {
        GeometryReader { geo in
            ZStack(alignment: .leading) {
                RoundedRectangle(cornerRadius: 4)
                    .fill(PianoFlowColors.surfaceDark)

                RoundedRectangle(cornerRadius: 3)
                    .fill(PianoFlowColors.brandPrimary.opacity(0.45))
                    .frame(width: geo.size.width * 0.35, height: geo.size.height)
                    .offset(x: min(max(0, viewModel.scrollOffset / 4.0), geo.size.width * 0.65))
            }
            .gesture(
                DragGesture()
                    .onChanged { value in
                        if dragStartOffset == nil {
                            dragStartOffset = viewModel.scrollOffset
                        }
                        let base = dragStartOffset ?? 0
                        viewModel.setScrollOffset(base + value.translation.width * 2.5)
                    }
                    .onEnded { _ in
                        dragStartOffset = nil
                    }
            )
        }
        .frame(height: 18)
    }

    private var volumeDialogSheet: some View {
        VStack(spacing: 16) {
            Text(L10n.tr("keyboard_volume"))
                .font(.headline)
                .foregroundColor(.white)

            Text("\(Int(viewModel.masterVolume * 100))%")
                .font(.title2.bold())
                .foregroundColor(PianoFlowColors.brandSecondary)

            Slider(
                value: Binding(
                    get: { Double(viewModel.masterVolume) },
                    set: { viewModel.setVolume(Float($0)) }
                ),
                in: 0...1
            )
            .tint(PianoFlowColors.brandPrimary)

            Button("OK") {
                showVolumeSheet = false
            }
            .font(.headline)
            .foregroundColor(PianoFlowColors.brandSecondary)
        }
        .padding(24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(PianoFlowColors.surfaceDark)
    }

    private var notationTitle: String {
        switch viewModel.notationType {
        case .cde: return L10n.tr("keyboard_label_cde")
        case .doReMi: return L10n.tr("keyboard_label_doremi")
        case .off: return L10n.tr("keyboard_label_off")
        }
    }
}
