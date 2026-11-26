package com.pianoflow.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = WhiteKeyColor,
    primaryContainer = BrandPrimaryDark,
    onPrimaryContainer = WhiteKeyColor,
    secondary = BrandSecondary,
    onSecondary = BackgroundDark,
    tertiary = BrandTertiary,
    onTertiary = WhiteKeyColor,
    background = BackgroundDark,
    onBackground = WhiteKeyColor,
    surface = SurfaceDark,
    onSurface = WhiteKeyColor,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = PurpleGrey80
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = WhiteKeyColor,
    primaryContainer = Purple80,
    onPrimaryContainer = BrandPrimaryDark,
    secondary = BrandSecondary,
    onSecondary = BackgroundDark,
    tertiary = BrandTertiary,
    onTertiary = WhiteKeyColor,
    background = BackgroundLight,
    onBackground = BackgroundDark,
    surface = SurfaceLight,
    onSurface = BackgroundDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = BackgroundDark
)

@Composable
fun PianoFlowTheme(
    darkTheme: Boolean = true, // Default to dark theme for authentic piano stage aesthetics
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
