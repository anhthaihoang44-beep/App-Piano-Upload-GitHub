import SwiftUI

public struct PaywallScreen: View {

    let billingRepository: BillingRepositoryProtocol
    let onDismiss: () -> Void
    let onNavigatePrivacy: () -> Void
    let onNavigateTerms: () -> Void

    @State private var selectedPlanId: String = "pianoflow_monthly"
    @State private var availablePlans: [SubscriptionPlan] = []
    @State private var isLoading: Bool = false

    public init(
        billingRepository: BillingRepositoryProtocol,
        onDismiss: @escaping () -> Void,
        onNavigatePrivacy: @escaping () -> Void,
        onNavigateTerms: @escaping () -> Void
    ) {
        self.billingRepository = billingRepository
        self.onDismiss = onDismiss
        self.onNavigatePrivacy = onNavigatePrivacy
        self.onNavigateTerms = onNavigateTerms
    }

    public var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                HStack {
                    Spacer()
                    Button(action: onDismiss) {
                        Image(systemName: "xmark")
                            .font(.headline)
                            .foregroundColor(.gray)
                            .padding(10)
                    }
                }

                ZStack {
                    Circle()
                        .fill(
                            RadialGradient(
                                colors: [PianoFlowColors.goldStarColor.opacity(0.3), .clear],
                                center: .center,
                                startRadius: 4,
                                endRadius: 44
                            )
                        )
                        .frame(width: 84, height: 84)
                    Image(systemName: "crown.fill")
                        .font(.system(size: 42))
                        .foregroundColor(PianoFlowColors.goldStarColor)
                }

                Text(L10n.tr("paywall_title"))
                    .font(.title2.bold())
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)

                Text(L10n.tr("paywall_subtitle"))
                    .font(.subheadline)
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.center)

                VStack(alignment: .leading, spacing: 10) {
                    FeatureCheckRow(text: L10n.tr("paywall_feature_songs"))
                    FeatureCheckRow(text: L10n.tr("paywall_feature_lessons"))
                    FeatureCheckRow(text: L10n.tr("paywall_feature_audio"))
                    FeatureCheckRow(text: L10n.tr("paywall_feature_no_limits"))
                }
                .padding(16)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(PianoFlowColors.surfaceDark)
                .clipShape(RoundedRectangle(cornerRadius: 16))

                VStack(spacing: 10) {
                    ForEach(availablePlans) { plan in
                        PlanCardView(
                            plan: plan,
                            isSelected: selectedPlanId == plan.id
                        ) {
                            selectedPlanId = plan.id
                        }
                    }
                }

                Button {
                    handleSubscribe()
                } label: {
                    Group {
                        if isLoading {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Text(L10n.tr("paywall_subscribe_now"))
                                .font(.headline.bold())
                                .foregroundColor(.white)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 54)
                    .background(PianoFlowColors.brandPrimary)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                }
                .disabled(isLoading)
                .padding(.top, 6)

                Button {
                    handleRestore()
                } label: {
                    Text(L10n.tr("paywall_restore"))
                        .font(.subheadline.weight(.medium))
                        .foregroundColor(PianoFlowColors.brandSecondary)
                }

                HStack(spacing: 8) {
                    Button(L10n.tr("profile_privacy"), action: onNavigatePrivacy)
                    Text("•")
                    Button(L10n.tr("profile_terms"), action: onNavigateTerms)
                }
                .font(.caption)
                .foregroundColor(.gray)
                .padding(.top, 4)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 24)
        }
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
        .onAppear {
            availablePlans = billingRepository.availablePlans
        }
        .onReceive(billingRepository.availablePlansPublisher) { plans in
            availablePlans = plans
        }
    }

    private func handleSubscribe() {
        Task {
            isLoading = true
            let purchased = await billingRepository.purchasePlan(selectedPlanId)
            isLoading = false
            if purchased {
                onDismiss()
            }
        }
    }

    private func handleRestore() {
        Task {
            isLoading = true
            let restored = await billingRepository.restorePurchases()
            isLoading = false
            if restored {
                onDismiss()
            }
        }
    }
}

private struct FeatureCheckRow: View {
    let text: String

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: "checkmark")
                .font(.subheadline.bold())
                .foregroundColor(PianoFlowColors.successGreen)
            Text(text)
                .font(.subheadline)
                .foregroundColor(.white)
        }
    }
}

private struct PlanCardView: View {
    let plan: SubscriptionPlan
    let isSelected: Bool
    let onSelect: () -> Void

    var body: some View {
        let localizedTitle: String = {
            switch plan.id {
            case "pianoflow_weekly": return L10n.tr("paywall_plan_weekly_title")
            case "pianoflow_monthly": return L10n.tr("paywall_plan_monthly_title")
            case "pianoflow_yearly": return L10n.tr("paywall_plan_yearly_title")
            default: return plan.title
            }
        }()
        let localizedPeriod: String = {
            switch plan.id {
            case "pianoflow_weekly": return L10n.tr("paywall_plan_weekly_period")
            case "pianoflow_monthly": return L10n.tr("paywall_plan_monthly_period")
            case "pianoflow_yearly": return L10n.tr("paywall_plan_yearly_period")
            default: return plan.period
            }
        }()

        Button(action: onSelect) {
            HStack {
                VStack(alignment: .leading, spacing: 3) {
                    HStack(spacing: 8) {
                        Text(localizedTitle)
                            .font(.headline.bold())
                            .foregroundColor(.white)

                        if plan.isRecommended {
                            Text(L10n.tr("paywall_popular_badge"))
                                .font(.system(size: 9, weight: .bold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(PianoFlowColors.brandPrimary)
                                .clipShape(RoundedRectangle(cornerRadius: 4))
                        }
                    }

                    Text(localizedPeriod)
                        .font(.caption)
                        .foregroundColor(.gray)
                }

                Spacer()

                Text(plan.priceFormatted)
                    .font(.title3.bold())
                    .foregroundColor(isSelected ? PianoFlowColors.brandSecondary : .white)
            }
            .padding(14)
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
