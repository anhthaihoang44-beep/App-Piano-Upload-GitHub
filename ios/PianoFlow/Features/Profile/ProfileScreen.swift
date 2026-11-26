import SwiftUI

public struct ProfileScreen: View {

    @ObservedObject var dataStore: PianoFlowDataStore
    @ObservedObject var preferencesRepository: UserPreferencesRepository
    let billingRepository: BillingRepositoryProtocol
    let onNavigatePaywall: () -> Void
    let onNavigatePrivacy: () -> Void
    let onNavigateTerms: () -> Void

    public init(
        dataStore: PianoFlowDataStore,
        preferencesRepository: UserPreferencesRepository,
        billingRepository: BillingRepositoryProtocol,
        onNavigatePaywall: @escaping () -> Void,
        onNavigatePrivacy: @escaping () -> Void,
        onNavigateTerms: @escaping () -> Void
    ) {
        self.dataStore = dataStore
        self.preferencesRepository = preferencesRepository
        self.billingRepository = billingRepository
        self.onNavigatePaywall = onNavigatePaywall
        self.onNavigatePrivacy = onNavigatePrivacy
        self.onNavigateTerms = onNavigateTerms
    }

    public var body: some View {
        let _ = preferencesRepository.appLanguage
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text(L10n.tr("profile_title"))
                    .font(.title.bold())
                    .foregroundColor(.white)
                    .padding(.top, 8)

                // VIP Membership Banner
                Button(action: onNavigatePaywall) {
                    HStack(spacing: 14) {
                        Image(systemName: "crown.fill")
                            .font(.title)
                            .foregroundColor(PianoFlowColors.goldStarColor)

                        VStack(alignment: .leading, spacing: 2) {
                            Text(preferencesRepository.isPremium ? L10n.tr("paywall_premium_active") : L10n.tr("paywall_title"))
                                .font(.headline.bold())
                                .foregroundColor(.white)
                            Text(preferencesRepository.isPremium ? L10n.tr("paywall_banner_active_sub") : L10n.tr("paywall_banner_inactive_sub"))
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.8))
                        }

                        Spacer()

                        Image(systemName: "chevron.right")
                            .foregroundColor(.white)
                    }
                    .padding(16)
                    .background(preferencesRepository.isPremium ? PianoFlowColors.surfaceElevatedDark : PianoFlowColors.brandPrimary)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                .buttonStyle(.plain)

                // Stats Grid
                VStack(spacing: 10) {
                    HStack(spacing: 10) {
                        StatCard(title: L10n.tr("profile_xp"), value: "\(dataStore.totalXp)", color: PianoFlowColors.brandSecondary)
                        StatCard(title: L10n.tr("profile_streak"), value: L10n.format("profile_streak_days", preferencesRepository.streakDays), color: .orange)
                    }
                    HStack(spacing: 10) {
                        StatCard(title: L10n.tr("profile_lessons_passed"), value: "\(dataStore.completedLessonsCount)", color: PianoFlowColors.brandPrimary)
                        StatCard(title: L10n.tr("profile_songs_played"), value: "\(dataStore.completedSongsCount)", color: PianoFlowColors.successGreen)
                    }
                }

                // Weekly Activity Chart
                VStack(alignment: .leading, spacing: 14) {
                    Text(L10n.tr("profile_weekly_activity"))
                        .font(.headline.bold())
                        .foregroundColor(.white)

                    HStack(alignment: .bottom) {
                        let days = [
                            L10n.tr("day_mon"),
                            L10n.tr("day_tue"),
                            L10n.tr("day_wed"),
                            L10n.tr("day_thu"),
                            L10n.tr("day_fri"),
                            L10n.tr("day_sat"),
                            L10n.tr("day_sun")
                        ]
                        let heights: [CGFloat] = [40, 65, 30, 80, 95, 60, 85]
                        ForEach(Array(days.enumerated()), id: \.offset) { idx, day in
                            Spacer()
                            VStack(spacing: 6) {
                                RoundedRectangle(cornerRadius: 6)
                                    .fill(idx == 6 ? PianoFlowColors.brandSecondary : PianoFlowColors.brandPrimary.opacity(0.5))
                                    .frame(width: 24, height: heights[idx])
                                Text(day)
                                    .font(.system(size: 10))
                                    .foregroundColor(.gray)
                            }
                            Spacer()
                        }
                    }
                }
                .padding(16)
                .background(PianoFlowColors.surfaceDark)
                .clipShape(RoundedRectangle(cornerRadius: 16))

                // Settings
                VStack(alignment: .leading, spacing: 10) {
                    Text(L10n.tr("profile_settings_header"))
                        .font(.headline.bold())
                        .foregroundColor(.white)

                    VStack(spacing: 0) {
                        // Language Switcher
                        HStack(spacing: 14) {
                            Image(systemName: "globe")
                                .foregroundColor(PianoFlowColors.brandSecondary)
                            Text(L10n.tr("profile_language"))
                                .font(.subheadline)
                                .foregroundColor(.white)
                            Spacer()
                            HStack(spacing: 4) {
                                ProfileLanguageChip(
                                    title: L10n.tr("profile_language_en"),
                                    selected: preferencesRepository.appLanguage == "en"
                                ) {
                                    L10n.currentLanguage = "en"
                                    preferencesRepository.setAppLanguage("en")
                                }
                                ProfileLanguageChip(
                                    title: L10n.tr("profile_language_vi"),
                                    selected: preferencesRepository.appLanguage == "vi"
                                ) {
                                    L10n.currentLanguage = "vi"
                                    preferencesRepository.setAppLanguage("vi")
                                }
                            }
                            .padding(3)
                            .background(PianoFlowColors.surfaceElevatedDark)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                        }
                        .padding(.vertical, 10)

                        Divider().background(PianoFlowColors.surfaceElevatedDark)

                        Toggle(isOn: Binding(
                            get: { preferencesRepository.dailyReminderEnabled },
                            set: { enabled in
                                preferencesRepository.setDailyReminderEnabled(enabled)
                                Task {
                                    if enabled {
                                        _ = await DailyPracticeNotificationManager.requestPermissionAndSchedule()
                                    } else {
                                        DailyPracticeNotificationManager.cancelReminder()
                                    }
                                }
                            }
                        )) {
                            HStack(spacing: 14) {
                                Image(systemName: "bell.fill")
                                    .foregroundColor(PianoFlowColors.brandSecondary)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(L10n.tr("profile_daily_reminder"))
                                        .font(.subheadline)
                                        .foregroundColor(.white)
                                    Text(L10n.tr("profile_reminder_time"))
                                        .font(.caption2)
                                        .foregroundColor(.gray)
                                }
                            }
                        }
                        .tint(PianoFlowColors.brandPrimary)
                        .padding(.vertical, 10)

                        Divider().background(PianoFlowColors.surfaceElevatedDark)

                        Toggle(isOn: Binding(
                            get: { preferencesRepository.hapticsEnabled },
                            set: { preferencesRepository.setHapticsEnabled($0) }
                        )) {
                            HStack(spacing: 14) {
                                Image(systemName: "iphone.radiowaves.left.and.right")
                                    .foregroundColor(PianoFlowColors.brandSecondary)
                                Text(L10n.tr("profile_haptics"))
                                    .font(.subheadline)
                                    .foregroundColor(.white)
                            }
                        }
                        .tint(PianoFlowColors.brandPrimary)
                        .padding(.vertical, 10)

                        Divider().background(PianoFlowColors.surfaceElevatedDark)

                        HStack(spacing: 14) {
                            Image(systemName: "gauge.with.dots.needle.67percent")
                                .foregroundColor(PianoFlowColors.brandSecondary)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(L10n.tr("profile_sound_latency"))
                                    .font(.subheadline)
                                    .foregroundColor(.white)
                                Text(L10n.tr("profile_sound_latency_desc"))
                                    .font(.caption2)
                                    .foregroundColor(.gray)
                            }
                            Spacer()
                        }
                        .padding(.vertical, 12)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 6)
                    .background(PianoFlowColors.surfaceDark)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }

                // Legal links
                VStack(spacing: 0) {
                    Button(action: onNavigatePrivacy) {
                        HStack(spacing: 14) {
                            Image(systemName: "hand.raised.fill")
                                .foregroundColor(.gray)
                            Text(L10n.tr("profile_privacy"))
                                .font(.subheadline)
                                .foregroundColor(.white)
                            Spacer()
                            Image(systemName: "chevron.right")
                                .foregroundColor(.gray)
                        }
                        .padding(.vertical, 12)
                    }

                    Divider().background(PianoFlowColors.surfaceElevatedDark)

                    Button(action: onNavigateTerms) {
                        HStack(spacing: 14) {
                            Image(systemName: "doc.text.fill")
                                .foregroundColor(.gray)
                            Text(L10n.tr("profile_terms"))
                                .font(.subheadline)
                                .foregroundColor(.white)
                            Spacer()
                            Image(systemName: "chevron.right")
                                .foregroundColor(.gray)
                        }
                        .padding(.vertical, 12)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 6)
                .background(PianoFlowColors.surfaceDark)
                .clipShape(RoundedRectangle(cornerRadius: 16))
            }
            .padding(16)
        }
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
    }
}

private struct ProfileLanguageChip: View {
    let title: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.caption.weight(.semibold))
                .foregroundColor(selected ? .white : .gray)
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .background(selected ? PianoFlowColors.brandPrimary : .clear)
                .clipShape(RoundedRectangle(cornerRadius: 8))
        }
        .buttonStyle(.plain)
    }
}

private struct StatCard: View {
    let title: String
    let value: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.caption2)
                .foregroundColor(.gray)
            Text(value)
                .font(.title3.bold())
                .foregroundColor(color)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(PianoFlowColors.surfaceDark)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}
