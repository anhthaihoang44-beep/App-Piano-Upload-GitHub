import SwiftUI

public enum MainTab: Hashable {
    case play
    case lessons
    case songs
    case recordings
    case profile
}

public enum AppRoute: Hashable {
    case doubleKeyboard
    case twoPlayerKeyboard
    case lessonPractice(String)
    case songPlayer(String)
    case recordingReplay(String)
    case paywall
    case privacyPolicy
    case termsOfService
}

public struct PianoFlowRootView: View {

    @ObservedObject var container: AppContainer
    @ObservedObject private var preferencesRepository: UserPreferencesRepository
    @StateObject private var keyboardViewModel: KeyboardViewModel

    @State private var selectedTab: MainTab = .play
    @State private var navigationPath = NavigationPath()

    public init(container: AppContainer) {
        self.container = container
        self.preferencesRepository = container.preferencesRepository
        _keyboardViewModel = StateObject(
            wrappedValue: KeyboardViewModel(
                audioEngine: container.audioEngine,
                preferencesRepository: container.preferencesRepository,
                dataStore: container.dataStore,
                hapticHelper: container.hapticHelper
            )
        )
    }

    public var body: some View {
        let _ = preferencesRepository.appLanguage
        Group {
            if !preferencesRepository.onboardingCompleted {
                OnboardingScreen(preferencesRepository: container.preferencesRepository) {
                    selectedTab = .play
                }
            } else {
                NavigationStack(path: $navigationPath) {
                    TabView(selection: $selectedTab) {
                        KeyboardScreen(
                            viewModel: keyboardViewModel,
                            onNavigateToDoubleKeyboard: { navigationPath.append(AppRoute.doubleKeyboard) },
                            onNavigateToTwoPlayer: { navigationPath.append(AppRoute.twoPlayerKeyboard) }
                        )
                        .tabItem {
                            Label(L10n.tr("nav_play"), systemImage: "music.note")
                        }
                        .tag(MainTab.play)

                        LessonsScreen(
                            dataStore: container.dataStore,
                            preferencesRepository: container.preferencesRepository,
                            billingRepository: container.billingRepository,
                            onLessonSelected: { lessonId in
                                navigationPath.append(AppRoute.lessonPractice(lessonId))
                            },
                            onPaywallRequired: {
                                navigationPath.append(AppRoute.paywall)
                            }
                        )
                        .tabItem {
                            Label(L10n.tr("nav_lessons"), systemImage: "graduationcap.fill")
                        }
                        .tag(MainTab.lessons)

                        SongListScreen(
                            dataStore: container.dataStore,
                            preferencesRepository: container.preferencesRepository,
                            billingRepository: container.billingRepository,
                            onSongSelected: { songId in
                                navigationPath.append(AppRoute.songPlayer(songId))
                            },
                            onPaywallRequired: {
                                navigationPath.append(AppRoute.paywall)
                            }
                        )
                        .tabItem {
                            Label(L10n.tr("nav_songs"), systemImage: "music.quarternote.3")
                        }
                        .tag(MainTab.songs)

                        RecordingListScreen(
                            dataStore: container.dataStore,
                            onReplayRecording: { recording in
                                navigationPath.append(AppRoute.recordingReplay(recording.recordingId))
                            }
                        )
                        .tabItem {
                            Label(L10n.tr("nav_recordings"), systemImage: "waveform")
                        }
                        .tag(MainTab.recordings)

                        ProfileScreen(
                            dataStore: container.dataStore,
                            preferencesRepository: container.preferencesRepository,
                            billingRepository: container.billingRepository,
                            onNavigatePaywall: { navigationPath.append(AppRoute.paywall) },
                            onNavigatePrivacy: { navigationPath.append(AppRoute.privacyPolicy) },
                            onNavigateTerms: { navigationPath.append(AppRoute.termsOfService) }
                        )
                        .tabItem {
                            Label(L10n.tr("nav_profile"), systemImage: "person.fill")
                        }
                        .tag(MainTab.profile)
                    }
                    .tint(PianoFlowColors.brandSecondary)
                    .toolbar(.hidden, for: .navigationBar)
                    .toolbarBackground(PianoFlowColors.surfaceDark, for: .tabBar)
                    .toolbarBackground(.visible, for: .tabBar)
                    .navigationDestination(for: AppRoute.self) { route in
                        destinationView(for: route)
                            .toolbar(.hidden, for: .navigationBar)
                    }
                }
            }
        }
        .environment(\.locale, Locale(identifier: preferencesRepository.appLanguage))
    }

    @ViewBuilder
    private func destinationView(for route: AppRoute) -> some View {
        switch route {
        case .doubleKeyboard:
            DoubleKeyboardScreen(viewModel: keyboardViewModel) {
                popRoute()
            }
        case .twoPlayerKeyboard:
            TwoPlayerKeyboardScreen(viewModel: keyboardViewModel) {
                popRoute()
            }
        case .lessonPractice(let lessonId):
            LessonPracticeScreen(
                lessonId: lessonId,
                audioEngine: container.audioEngine,
                dataStore: container.dataStore,
                onBack: { popRoute() }
            )
        case .songPlayer(let songId):
            SongPlayerScreen(
                songId: songId,
                audioEngine: container.audioEngine,
                dataStore: container.dataStore,
                onBack: { popRoute() }
            )
        case .recordingReplay(let recordingId):
            if let recording = container.dataStore.recordings.first(where: { $0.recordingId == recordingId }) {
                RecordingReplayScreen(
                    recording: recording,
                    audioEngine: container.audioEngine,
                    onBack: { popRoute() }
                )
            } else {
                Text("Recording not found")
                    .foregroundColor(.white)
            }
        case .paywall:
            PaywallScreen(
                billingRepository: container.billingRepository,
                onDismiss: { popRoute() },
                onNavigatePrivacy: { navigationPath.append(AppRoute.privacyPolicy) },
                onNavigateTerms: { navigationPath.append(AppRoute.termsOfService) }
            )
        case .privacyPolicy:
            PrivacyPolicyScreen {
                popRoute()
            }
        case .termsOfService:
            TermsOfServiceScreen {
                popRoute()
            }
        }
    }

    private func popRoute() {
        if !navigationPath.isEmpty {
            navigationPath.removeLast()
        }
    }
}
