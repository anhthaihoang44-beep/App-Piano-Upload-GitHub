import Combine
import Foundation
import StoreKit

private final class StoreKitTaskHandle: @unchecked Sendable {
    var task: Task<Void, Never>?
    deinit {
        task?.cancel()
    }
}

/// Production StoreKit 2 billing repository for App Store subscriptions.
/// Swap `MockBillingRepository` with `StoreKitBillingRepository` in `AppContainer.swift` for release builds.
@MainActor
public final class StoreKitBillingRepository: BillingRepositoryProtocol {

    public static let productIds: [String] = [
        "pianoflow_weekly",
        "pianoflow_monthly",
        "pianoflow_yearly"
    ]

    private let preferencesRepository: UserPreferencesRepository
    private let isPremiumSubject = CurrentValueSubject<Bool, Never>(false)
    private let availablePlansSubject = CurrentValueSubject<[SubscriptionPlan], Never>(MockBillingRepository.defaultPlans)

    private var storeProducts: [String: Product] = [:]
    private let updatesTaskHandle = StoreKitTaskHandle()

    public var isPremium: Bool { isPremiumSubject.value }
    public var availablePlans: [SubscriptionPlan] { availablePlansSubject.value }
    public var isPremiumPublisher: AnyPublisher<Bool, Never> { isPremiumSubject.eraseToAnyPublisher() }
    public var availablePlansPublisher: AnyPublisher<[SubscriptionPlan], Never> { availablePlansSubject.eraseToAnyPublisher() }

    public init(preferencesRepository: UserPreferencesRepository) {
        self.preferencesRepository = preferencesRepository
        self.isPremiumSubject.send(preferencesRepository.isPremium)
        listenForTransactionUpdates()
    }

    public func startConnection() async {
        await loadProducts()
        await refreshEntitlements()
    }

    public func purchasePlan(_ planId: String) async -> Bool {
        if storeProducts[planId] == nil {
            await loadProducts()
        }
        guard let product = storeProducts[planId] else {
            return false
        }

        do {
            let result = try await product.purchase()
            switch result {
            case .success(let verification):
                let transaction = try checkVerified(verification)
                await transaction.finish()
                await refreshEntitlements()
                return true
            case .userCancelled, .pending:
                return false
            @unknown default:
                return false
            }
        } catch {
            return false
        }
    }

    public func restorePurchases() async -> Bool {
        do {
            try await AppStore.sync()
            await refreshEntitlements()
            return isPremiumSubject.value
        } catch {
            return false
        }
    }

    private func loadProducts() async {
        do {
            let products = try await Product.products(for: Self.productIds)
            guard !products.isEmpty else { return }

            var productMap: [String: Product] = [:]
            for product in products {
                productMap[product.id] = product
            }
            self.storeProducts = productMap

            let plans = Self.productIds.compactMap { id -> SubscriptionPlan? in
                guard let product = productMap[id] else {
                    return MockBillingRepository.defaultPlans.first { $0.id == id }
                }
                let template = MockBillingRepository.defaultPlans.first { $0.id == id }
                return SubscriptionPlan(
                    id: product.id,
                    title: product.displayName.isEmpty ? (template?.title ?? product.id) : product.displayName,
                    priceFormatted: product.displayPrice,
                    period: template?.period ?? product.description,
                    isRecommended: id == "pianoflow_monthly",
                    features: template?.features ?? []
                )
            }
            availablePlansSubject.send(plans)
        } catch {
            // Retain default fallback plans if StoreKit products cannot be fetched
        }
    }

    private func refreshEntitlements() async {
        var hasActiveSubscription = false
        for await result in Transaction.currentEntitlements {
            if let transaction = try? checkVerified(result),
               Self.productIds.contains(transaction.productID),
               transaction.revocationDate == nil {
                hasActiveSubscription = true
                break
            }
        }
        isPremiumSubject.send(hasActiveSubscription)
        preferencesRepository.setPremium(hasActiveSubscription)
    }

    private func listenForTransactionUpdates() {
        updatesTaskHandle.task = Task { [weak self] in
            for await result in Transaction.updates {
                guard let self else { return }
                if let transaction = try? self.checkVerified(result) {
                    await transaction.finish()
                    await self.refreshEntitlements()
                }
            }
        }
    }

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .verified(let safe):
            return safe
        case .unverified(_, let error):
            throw error
        }
    }
}
