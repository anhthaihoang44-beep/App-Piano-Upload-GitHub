import Combine
import Foundation

@MainActor
public final class MockBillingRepository: BillingRepositoryProtocol {

    private let preferencesRepository: UserPreferencesRepository
    private let isPremiumSubject = CurrentValueSubject<Bool, Never>(false)
    private let availablePlansSubject = CurrentValueSubject<[SubscriptionPlan], Never>([])
    private var cancellable: AnyCancellable?

    public var isPremium: Bool { isPremiumSubject.value }
    public var availablePlans: [SubscriptionPlan] { availablePlansSubject.value }
    public var isPremiumPublisher: AnyPublisher<Bool, Never> { isPremiumSubject.eraseToAnyPublisher() }
    public var availablePlansPublisher: AnyPublisher<[SubscriptionPlan], Never> { availablePlansSubject.eraseToAnyPublisher() }

    public static let defaultPlans: [SubscriptionPlan] = [
        SubscriptionPlan(
            id: "pianoflow_weekly",
            title: "Weekly VIP",
            priceFormatted: "$2.99",
            period: "per week",
            isRecommended: false,
            features: ["All 50+ songs unlocked", "Unlimited recording playback"]
        ),
        SubscriptionPlan(
            id: "pianoflow_monthly",
            title: "Monthly VIP",
            priceFormatted: "$7.99",
            period: "per month",
            isRecommended: true,
            features: ["All songs & master lessons", "High-fidelity audio export", "7-Day Free Trial included"]
        ),
        SubscriptionPlan(
            id: "pianoflow_yearly",
            title: "Yearly VIP",
            priceFormatted: "$39.99",
            period: "per year (Save 60%)",
            isRecommended: false,
            features: ["Best long-term value", "All future songs and features", "VIP priority support"]
        )
    ]

    public init(preferencesRepository: UserPreferencesRepository) {
        self.preferencesRepository = preferencesRepository
        self.isPremiumSubject.send(preferencesRepository.isPremium)
        self.availablePlansSubject.send(Self.defaultPlans)

        self.cancellable = preferencesRepository.$isPremium.sink { [weak self] premium in
            self?.isPremiumSubject.send(premium)
        }
    }

    public func startConnection() async {
        // Mock connection ready immediately for local development
    }

    public func purchasePlan(_ planId: String) async -> Bool {
        try? await Task.sleep(nanoseconds: 500_000_000)
        preferencesRepository.setPremium(true)
        isPremiumSubject.send(true)
        return true
    }

    public func restorePurchases() async -> Bool {
        try? await Task.sleep(nanoseconds: 400_000_000)
        preferencesRepository.setPremium(true)
        isPremiumSubject.send(true)
        return true
    }
}
