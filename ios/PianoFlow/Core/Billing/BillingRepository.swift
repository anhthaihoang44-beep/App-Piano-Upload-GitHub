import Combine
import Foundation

public struct SubscriptionPlan: Identifiable, Hashable, Sendable {
    public let id: String
    public let title: String
    public let priceFormatted: String
    public let period: String
    public let isRecommended: Bool
    public let features: [String]

    public init(
        id: String,
        title: String,
        priceFormatted: String,
        period: String,
        isRecommended: Bool = false,
        features: [String] = []
    ) {
        self.id = id
        self.title = title
        self.priceFormatted = priceFormatted
        self.period = period
        self.isRecommended = isRecommended
        self.features = features
    }
}

@MainActor
public protocol BillingRepositoryProtocol: AnyObject {
    var isPremium: Bool { get }
    var availablePlans: [SubscriptionPlan] { get }
    var isPremiumPublisher: AnyPublisher<Bool, Never> { get }
    var availablePlansPublisher: AnyPublisher<[SubscriptionPlan], Never> { get }

    func startConnection() async
    func purchasePlan(_ planId: String) async -> Bool
    func restorePurchases() async -> Bool
}
