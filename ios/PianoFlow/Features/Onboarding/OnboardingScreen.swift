import SwiftUI

public struct OnboardingScreen: View {

    @ObservedObject var preferencesRepository: UserPreferencesRepository
    let onComplete: () -> Void

    @State private var currentStep: Int = 0
    @State private var selectedSkillLevel: String = "Beginner"
    @State private var selectedDailyMinutes: Int = 15

    public init(
        preferencesRepository: UserPreferencesRepository,
        onComplete: @escaping () -> Void
    ) {
        self.preferencesRepository = preferencesRepository
        self.onComplete = onComplete
    }

    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Spacer()
                HStack(spacing: 4) {
                    LanguagePillButton(
                        title: "EN",
                        selected: preferencesRepository.appLanguage == "en"
                    ) {
                        L10n.currentLanguage = "en"
                        preferencesRepository.setAppLanguage("en")
                    }
                    LanguagePillButton(
                        title: "VI",
                        selected: preferencesRepository.appLanguage == "vi"
                    ) {
                        L10n.currentLanguage = "vi"
                        preferencesRepository.setAppLanguage("vi")
                    }
                }
                .padding(3)
                .background(PianoFlowColors.surfaceDark)
                .clipShape(Capsule())
            }

            Spacer().frame(height: 16)

            HStack(spacing: 8) {
                ForEach(0..<3, id: \.self) { index in
                    Circle()
                        .fill(index == currentStep ? PianoFlowColors.brandSecondary : PianoFlowColors.surfaceElevatedDark)
                        .frame(width: index == currentStep ? 10 : 8, height: index == currentStep ? 10 : 8)
                }
            }

            Spacer().frame(height: 36)

            if currentStep == 0 {
                ZStack {
                    Circle()
                        .fill(PianoFlowColors.brandPrimary.opacity(0.2))
                        .frame(width: 110, height: 110)
                    Image(systemName: "music.note")
                        .font(.system(size: 54))
                        .foregroundColor(PianoFlowColors.brandSecondary)
                }

                Spacer().frame(height: 28)

                Text(L10n.tr("onboarding_step1_title"))
                    .font(.largeTitle.bold())
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 12)

                Text(L10n.tr("onboarding_step1_desc"))
                    .font(.body)
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.center)
            } else if currentStep == 1 {
                Text(L10n.tr("onboarding_step2_title"))
                    .font(.title2.bold())
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 24)

                let levels: [(String, String, String)] = [
                    ("Beginner", L10n.tr("onboarding_level_beginner_title"), L10n.tr("onboarding_level_beginner")),
                    ("Intermediate", L10n.tr("onboarding_level_intermediate_title"), L10n.tr("onboarding_level_intermediate")),
                    ("Advanced", L10n.tr("onboarding_level_advanced_title"), L10n.tr("onboarding_level_advanced"))
                ]

                VStack(spacing: 12) {
                    ForEach(levels, id: \.0) { key, title, desc in
                        SelectableOptionCard(
                            title: title,
                            subtitle: desc,
                            isSelected: selectedSkillLevel == key
                        ) {
                            selectedSkillLevel = key
                        }
                    }
                }
            } else {
                Text(L10n.tr("onboarding_step3_title"))
                    .font(.title2.bold())
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 24)

                let goals: [(Int, String)] = [
                    (5, L10n.tr("onboarding_goal_casual")),
                    (15, L10n.tr("onboarding_goal_regular")),
                    (30, L10n.tr("onboarding_goal_serious"))
                ]

                VStack(spacing: 12) {
                    ForEach(goals, id: \.0) { mins, desc in
                        SelectableOptionCard(
                            title: L10n.format("onboarding_goal_min_per_day", mins),
                            subtitle: desc,
                            isSelected: selectedDailyMinutes == mins
                        ) {
                            selectedDailyMinutes = mins
                        }
                    }
                }
            }

            Spacer()

            Button {
                handleNext()
            } label: {
                Text(currentStep == 2 ? L10n.tr("onboarding_get_started") : L10n.tr("common_next"))
                    .font(.headline.bold())
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 54)
                    .background(PianoFlowColors.brandPrimary)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
            }

            Spacer().frame(height: 16)
        }
        .padding(24)
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
    }

    private func handleNext() {
        if currentStep < 2 {
            currentStep += 1
        } else {
            preferencesRepository.setSkillLevel(selectedSkillLevel)
            preferencesRepository.setDailyGoalMinutes(selectedDailyMinutes)
            preferencesRepository.setOnboardingCompleted(true)
            onComplete()
        }
    }
}

private struct SelectableOptionCard: View {
    let title: String
    let subtitle: String
    let isSelected: Bool
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.headline.bold())
                    .foregroundColor(isSelected ? PianoFlowColors.brandSecondary : .white)
                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(16)
            .background(isSelected ? PianoFlowColors.surfaceElevatedDark : PianoFlowColors.surfaceDark)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(isSelected ? PianoFlowColors.brandSecondary : .clear, lineWidth: 2)
            )
        }
        .buttonStyle(.plain)
    }
}

private struct LanguagePillButton: View {
    let title: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.caption.bold())
                .foregroundColor(selected ? .white : .gray)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(selected ? PianoFlowColors.brandPrimary : .clear)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}
