package com.pianoflow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector
import com.pianoflow.app.R

sealed class Screen(val route: String) {
    object Play : Screen("play")
    object Lessons : Screen("lessons")
    object Songs : Screen("songs")
    object Recordings : Screen("recordings")
    object Profile : Screen("profile")

    object SongPlayer : Screen("song_player/{songId}") {
        fun createRoute(songId: String) = "song_player/$songId"
    }

    object LessonPractice : Screen("lesson_practice/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson_practice/$lessonId"
    }

    object Paywall : Screen("paywall")
    object Onboarding : Screen("onboarding")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfService : Screen("terms_of_service")
    object DoubleKeyboard : Screen("double_keyboard")
    object TwoPlayerKeyboard : Screen("two_player_keyboard")
}

data class BottomNavItem(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector
)

val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(Screen.Play.route, R.string.nav_play, Icons.Default.MusicNote),
    BottomNavItem(Screen.Lessons.route, R.string.nav_lessons, Icons.Default.School),
    BottomNavItem(Screen.Songs.route, R.string.nav_songs, Icons.Default.Audiotrack),
    BottomNavItem(Screen.Recordings.route, R.string.nav_recordings, Icons.Default.GraphicEq),
    BottomNavItem(Screen.Profile.route, R.string.nav_profile, Icons.Default.Person)
)
