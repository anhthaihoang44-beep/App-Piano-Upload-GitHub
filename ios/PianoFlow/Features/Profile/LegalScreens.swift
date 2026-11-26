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

                Text("PianoFlow Privacy Policy")
                    .font(.title3.bold())
                    .foregroundColor(.white)

                Text("""
Last updated: September 2026

Your privacy is important to us. PianoFlow does not collect, sell, or transmit any personal data or recorded melodies to external servers without your permission.

1. Local Storage: All song scores, lesson progress, and recorded piano files are stored securely on your local device using Apple SwiftData and UserDefaults.

2. Audio & Recordings: Audio recordings are processed on-device and only exported or shared when you explicitly initiate the iOS Share Sheet.

3. Subscriptions & Billing: In-app purchases are securely processed through Apple StoreKit 2 and the App Store. We do not store your credit card or payment credentials.

4. Contact: For privacy questions, contact support@pianoflow.app.
""")
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

                Text("PianoFlow Terms of Service")
                    .font(.title3.bold())
                    .foregroundColor(.white)

                Text("""
Last updated: September 2026

By downloading or using PianoFlow, you agree to the following terms:

1. License: PianoFlow grants you a revocable, non-exclusive, non-transferable personal license to learn and play piano for personal entertainment and educational use.

2. Songs & Intellectual Property: All musical arrangements included within the application are in the public domain or independently created. You own full rights to original songs you compose and record using PianoFlow.

3. Subscriptions: PianoFlow Premium provides recurring weekly, monthly, and yearly options. Subscriptions automatically renew unless cancelled at least 24 hours prior to the end of the billing period via your Apple ID subscription settings.

4. Disclaimer: The software is provided 'as is' without warranties of any kind.
""")
                .font(.subheadline)
                .foregroundColor(.gray)
            }
            .padding(16)
        }
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
    }
}
