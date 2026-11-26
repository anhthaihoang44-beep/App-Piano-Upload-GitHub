import Combine
import Foundation

@MainActor
public final class UserPreferencesRepository: ObservableObject {

    private enum Keys {
        static let notationType = "notation_type"
        static let sustainEnabled = "sustain_enabled"
        static let volume = "volume"
        static let hapticsEnabled = "haptics_enabled"
        static let onboardingCompleted = "onboarding_completed"
        static let skillLevel = "skill_level"
        static let dailyGoalMinutes = "daily_goal_minutes"
        static let dailyReminderEnabled = "daily_reminder_enabled"
        static let streakDays = "streak_days"
        static let isPremium = "is_premium"
        static let visibleKeysCount = "visible_keys_count"
    }

    private let defaults: UserDefaults

    @Published public private(set) var notationType: NotationType
    @Published public private(set) var sustainEnabled: Bool
    @Published public private(set) var volume: Float
    @Published public private(set) var hapticsEnabled: Bool
    @Published public private(set) var onboardingCompleted: Bool
    @Published public private(set) var skillLevel: String
    @Published public private(set) var dailyGoalMinutes: Int
    @Published public private(set) var dailyReminderEnabled: Bool
    @Published public private(set) var streakDays: Int
    @Published public private(set) var isPremium: Bool
    @Published public private(set) var visibleKeysCount: Int

    public init(defaults: UserDefaults = .standard) {
        self.defaults = defaults

        if let rawNotation = defaults.string(forKey: Keys.notationType),
           let parsed = NotationType(rawValue: rawNotation) {
            self.notationType = parsed
        } else {
            self.notationType = .cde
        }

        self.sustainEnabled = defaults.object(forKey: Keys.sustainEnabled) as? Bool ?? false
        self.volume = defaults.object(forKey: Keys.volume) as? Float ?? 0.9
        self.hapticsEnabled = defaults.object(forKey: Keys.hapticsEnabled) as? Bool ?? true
        self.onboardingCompleted = defaults.object(forKey: Keys.onboardingCompleted) as? Bool ?? false
        self.skillLevel = defaults.string(forKey: Keys.skillLevel) ?? "Beginner"
        self.dailyGoalMinutes = defaults.object(forKey: Keys.dailyGoalMinutes) as? Int ?? 15
        self.dailyReminderEnabled = defaults.object(forKey: Keys.dailyReminderEnabled) as? Bool ?? true
        self.streakDays = defaults.object(forKey: Keys.streakDays) as? Int ?? 1
        self.isPremium = defaults.object(forKey: Keys.isPremium) as? Bool ?? false
        self.visibleKeysCount = defaults.object(forKey: Keys.visibleKeysCount) as? Int ?? 14
    }

    public func setNotationType(_ type: NotationType) {
        notationType = type
        defaults.set(type.rawValue, forKey: Keys.notationType)
    }

    public func setSustainEnabled(_ enabled: Bool) {
        sustainEnabled = enabled
        defaults.set(enabled, forKey: Keys.sustainEnabled)
    }

    public func setVolume(_ value: Float) {
        let clamped = min(max(value, 0.0), 1.0)
        volume = clamped
        defaults.set(clamped, forKey: Keys.volume)
    }

    public func setHapticsEnabled(_ enabled: Bool) {
        hapticsEnabled = enabled
        defaults.set(enabled, forKey: Keys.hapticsEnabled)
    }

    public func setOnboardingCompleted(_ completed: Bool) {
        onboardingCompleted = completed
        defaults.set(completed, forKey: Keys.onboardingCompleted)
    }

    public func setSkillLevel(_ level: String) {
        skillLevel = level
        defaults.set(level, forKey: Keys.skillLevel)
    }

    public func setDailyGoalMinutes(_ minutes: Int) {
        dailyGoalMinutes = minutes
        defaults.set(minutes, forKey: Keys.dailyGoalMinutes)
    }

    public func setDailyReminderEnabled(_ enabled: Bool) {
        dailyReminderEnabled = enabled
        defaults.set(enabled, forKey: Keys.dailyReminderEnabled)
    }

    public func setStreakDays(_ days: Int) {
        streakDays = days
        defaults.set(days, forKey: Keys.streakDays)
    }

    public func setPremium(_ premium: Bool) {
        isPremium = premium
        defaults.set(premium, forKey: Keys.isPremium)
    }

    public func setVisibleKeysCount(_ count: Int) {
        let clamped = min(max(count, 7), 28)
        visibleKeysCount = clamped
        defaults.set(clamped, forKey: Keys.visibleKeysCount)
    }
}
