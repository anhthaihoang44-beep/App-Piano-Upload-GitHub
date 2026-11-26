import SwiftUI

public struct TwoPlayerKeyboardScreen: View {

    @ObservedObject var viewModel: KeyboardViewModel
    let onBack: () -> Void

    public init(viewModel: KeyboardViewModel, onBack: @escaping () -> Void) {
        self.viewModel = viewModel
        self.onBack = onBack
    }

    public var body: some View {
        VStack(spacing: 0) {
            // Player 1 Keyboard (Rotated 180 degrees for face-to-face duet)
            PianoCanvasView(
                startMidi: 48,
                endMidi: 72,
                keyWidthPt: viewModel.keyWidthPt * 0.95,
                notationType: viewModel.notationType,
                onKeyDown: { viewModel.onKeyDown($0) },
                onKeyUp: { viewModel.onKeyUp($0) }
            )
            .rotationEffect(.degrees(180))
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            // Center Divider Bar
            HStack {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.title3.bold())
                        .foregroundColor(.white)
                        .padding(6)
                }

                Text("Player 1 (Top)")
                    .font(.caption2.bold())
                    .foregroundColor(PianoFlowColors.brandTertiary)

                Spacer()

                Text(L10n.tr("keyboard_mode_duet"))
                    .font(.subheadline.bold())
                    .foregroundColor(.white)

                Spacer()

                Text("Player 2 (Bottom)")
                    .font(.caption2.bold())
                    .foregroundColor(PianoFlowColors.brandSecondary)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(PianoFlowColors.surfaceVariantDark)

            // Player 2 Keyboard (Normal orientation)
            PianoCanvasView(
                startMidi: 48,
                endMidi: 72,
                keyWidthPt: viewModel.keyWidthPt * 0.95,
                notationType: viewModel.notationType,
                onKeyDown: { viewModel.onKeyDown($0) },
                onKeyUp: { viewModel.onKeyUp($0) }
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
    }
}
