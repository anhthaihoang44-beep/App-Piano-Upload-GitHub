import SwiftUI

public struct PrivacyPolicyScreen: View {
    let onBack: () -> Void

    public init(onBack: @escaping () -> Void) {
        self.onBack = onBack
    }

    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                HStack {
                    Button(action: onBack) {
                        Image(systemName: "chevron.left")
                            .font(.title3.bold())
                            .foregroundColor(.white)
                            .padding(8)
                    }
                    Text(L10n.tr("profile_privacy"))
                        .font(.headline.bold())
                        .foregroundColor(.white)
                    Spacer()
                }

                Text(L10n.tr("legal_privacy_heading"))
                    .font(.title3.bold())
                    .foregroundColor(.white)

                Text(L10n.tr("legal_privacy_body"))
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }
            .padding(16)
        }
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
    }
}

public struct TermsOfServiceScreen: View {
    let onBack: () -> Void

    public init(onBack: @escaping () -> Void) {
        self.onBack = onBack
    }

    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                HStack {
                    Button(action: onBack) {
                        Image(systemName: "chevron.left")
                            .font(.title3.bold())
                            .foregroundColor(.white)
                            .padding(8)
                    }
                    Text(L10n.tr("profile_terms"))
                        .font(.headline.bold())
                        .foregroundColor(.white)
                    Spacer()
                }

                Text(L10n.tr("legal_terms_heading"))
                    .font(.title3.bold())
                    .foregroundColor(.white)

                Text(L10n.tr("legal_terms_body"))
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }
            .padding(16)
        }
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
    }
}
