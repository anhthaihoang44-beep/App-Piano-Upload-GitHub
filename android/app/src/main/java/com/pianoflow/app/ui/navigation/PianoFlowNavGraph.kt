package com.pianoflow.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pianoflow.app.PianoFlowApplication
import com.pianoflow.app.core.database.entity.RecordingEntity
import com.pianoflow.app.feature.keyboard.KeyboardScreen
import com.pianoflow.app.feature.keyboard.KeyboardViewModel
import com.pianoflow.app.feature.keyboard.components.DoubleKeyboardScreen
import com.pianoflow.app.feature.keyboard.components.TwoPlayerKeyboardScreen
import com.pianoflow.app.feature.lessons.LessonPracticeScreen
import com.pianoflow.app.feature.lessons.LessonsScreen
import com.pianoflow.app.feature.onboarding.OnboardingScreen
import com.pianoflow.app.feature.paywall.PaywallScreen
import com.pianoflow.app.feature.profile.PrivacyPolicyScreen
import com.pianoflow.app.feature.profile.ProfileScreen
import com.pianoflow.app.feature.profile.TermsOfServiceScreen
import com.pianoflow.app.feature.recording.RecordingListScreen
import com.pianoflow.app.feature.recording.RecordingReplayScreen
import com.pianoflow.app.feature.songs.SongListScreen
import com.pianoflow.app.feature.songs.SongPlayerScreen
import com.pianoflow.app.feature.songs.SongPlayerViewModel
import com.pianoflow.app.ui.components.BottomNavBar

@Composable
fun PianoFlowNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val app = PianoFlowApplication.instance
    val onboardingCompleted by app.preferencesRepository.onboardingCompleted.collectAsState(initial = true)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screen.Play.route,
        Screen.Lessons.route,
        Screen.Songs.route,
        Screen.Recordings.route,
        Screen.Profile.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    // Global keyboard viewmodel instance
    val keyboardViewModel: KeyboardViewModel = remember {
        KeyboardViewModel(
            audioEngine = app.audioEngine,
            preferencesRepository = app.preferencesRepository,
            database = app.database,
            hapticHelper = app.hapticHelper
        )
    }

    // Active replay recording state
    var activeReplayRecording by remember { mutableStateOf<RecordingEntity?>(null) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Play.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (!onboardingCompleted) Screen.Onboarding.route else Screen.Play.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    preferencesRepository = app.preferencesRepository,
                    onComplete = {
                        navController.navigate(Screen.Play.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Play.route) {
                KeyboardScreen(
                    viewModel = keyboardViewModel,
                    onNavigateToDoubleKeyboard = {
                        navController.navigate(Screen.DoubleKeyboard.route)
                    },
                    onNavigateToTwoPlayer = {
                        navController.navigate(Screen.TwoPlayerKeyboard.route)
                    }
                )
            }

            composable(Screen.DoubleKeyboard.route) {
                DoubleKeyboardScreen(
                    viewModel = keyboardViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.TwoPlayerKeyboard.route) {
                TwoPlayerKeyboardScreen(
                    viewModel = keyboardViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Lessons.route) {
                LessonsScreen(
                    database = app.database,
                    billingRepository = app.billingRepository,
                    preferencesRepository = app.preferencesRepository,
                    onLessonSelected = { lessonId ->
                        navController.navigate(Screen.LessonPractice.createRoute(lessonId))
                    },
                    onPaywallRequired = {
                        navController.navigate(Screen.Paywall.route)
                    }
                )
            }

            composable(
                route = Screen.LessonPractice.route,
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
            ) { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                LessonPracticeScreen(
                    lessonId = lessonId,
                    audioEngine = app.audioEngine,
                    database = app.database,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Songs.route) {
                SongListScreen(
                    database = app.database,
                    billingRepository = app.billingRepository,
                    onSongSelected = { songId ->
                        navController.navigate(Screen.SongPlayer.createRoute(songId))
                    },
                    onPaywallRequired = {
                        navController.navigate(Screen.Paywall.route)
                    }
                )
            }

            composable(
                route = Screen.SongPlayer.route,
                arguments = listOf(navArgument("songId") { type = NavType.StringType })
            ) { backStackEntry ->
                val songId = backStackEntry.arguments?.getString("songId") ?: ""
                val songViewModel = remember(songId) {
                    SongPlayerViewModel(
                        songId = songId,
                        audioEngine = app.audioEngine,
                        database = app.database
                    )
                }
                SongPlayerScreen(
                    viewModel = songViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Recordings.route) {
                if (activeReplayRecording != null) {
                    RecordingReplayScreen(
                        recording = activeReplayRecording!!,
                        audioEngine = app.audioEngine,
                        onBack = { activeReplayRecording = null }
                    )
                } else {
                    RecordingListScreen(
                        database = app.database,
                        onReplayRecording = { recording ->
                            activeReplayRecording = recording
                        }
                    )
                }
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    database = app.database,
                    billingRepository = app.billingRepository,
                    preferencesRepository = app.preferencesRepository,
                    onNavigatePaywall = { navController.navigate(Screen.Paywall.route) },
                    onNavigatePrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                    onNavigateTerms = { navController.navigate(Screen.TermsOfService.route) }
                )
            }

            composable(Screen.Paywall.route) {
                PaywallScreen(
                    billingRepository = app.billingRepository,
                    onDismiss = { navController.popBackStack() },
                    onNavigatePrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                    onNavigateTerms = { navController.navigate(Screen.TermsOfService.route) }
                )
            }

            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.TermsOfService.route) {
                TermsOfServiceScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
