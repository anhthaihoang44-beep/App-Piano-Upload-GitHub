import Combine
import Foundation

/// Central dependency container for PianoFlow iOS (Clean Architecture + MVVM).
@MainActor
public final class AppContainer: ObservableObject {

    public let dataStore: PianoFlowDataStore
    public let preferencesRepository: UserPreferencesRepository
    public let audioEngine: AudioEngineProtocol
    public let billingRepository: BillingRepositoryProtocol
    public let hapticHelper: HapticHelper

    private var didBootstrap = false

    public init(inMemoryPersistence: Bool = false) {
        let store = PianoFlowDataStore(inMemory: inMemoryPersistence)
        let prefs = UserPreferencesRepository()
        let audio = AVAudioPianoEngine()
        // Swap MockBillingRepository for StoreKitBillingRepository when shipping to App Store Connect.
        let billing: BillingRepositoryProtocol = MockBillingRepository(preferencesRepository: prefs)
        let haptics = HapticHelper()

        self.dataStore = store
        self.preferencesRepository = prefs
        self.audioEngine = audio
        self.billingRepository = billing
        self.hapticHelper = haptics
    }

    public func bootstrap() async {
        guard !didBootstrap else { return }
        didBootstrap = true

        await audioEngine.initialize()
        await billingRepository.startConnection()

        if preferencesRepository.dailyReminderEnabled {
            await DailyPracticeNotificationManager.scheduleDailyReminder()
        }
    }
}
