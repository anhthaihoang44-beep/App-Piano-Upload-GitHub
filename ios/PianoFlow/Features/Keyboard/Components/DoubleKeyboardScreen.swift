import SwiftUI

public struct DoubleKeyboardScreen: View {

    @ObservedObject var viewModel: KeyboardViewModel
    let onBack: () -> Void

    public init(viewModel: KeyboardViewModel, onBack: @escaping () -> Void) {
        self.viewModel = viewModel
        self.onBack = onBack
    }

    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.title3.bold())
                        .foregroundColor(.white)
                        .padding(8)
                }

                Text(L10n.tr("keyboard_mode_double"))
                    .font(.headline)
                    .foregroundColor(.white)

                Spacer()

                Text(L10n.tr("keyboard_double_range"))
                    .font(.caption2)
                    .foregroundColor(PianoFlowColors.brandSecondary)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)

            // Upper Keyboard (Treble / Right Hand)
            PianoCanvasView(
                startMidi: 60,
                endMidi: 84,
                keyWidthPt: viewModel.keyWidthPt * 0.9,
                notationType: viewModel.notationType,
                onKeyDown: { viewModel.onKeyDown($0) },
                onKeyUp: { viewModel.onKeyUp($0) }
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            Rectangle()
                .fill(PianoFlowColors.brandPrimary)
                .frame(height: 3)

            // Lower Keyboard (Bass / Left Hand)
            PianoCanvasView(
                startMidi: 36,
                endMidi: 60,
                keyWidthPt: viewModel.keyWidthPt * 0.9,
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
