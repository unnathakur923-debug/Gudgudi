package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GudgudiDarkColorScheme = darkColorScheme(
    primary = GudgudiGold,
    onPrimary = GudgudiMaroonDark,
    primaryContainer = GudgudiCrimson,
    onPrimaryContainer = GudgudiGoldLight,
    secondary = GudgudiGoldAmber,
    onSecondary = GudgudiMaroonDark,
    secondaryContainer = GudgudiMaroonCard,
    onSecondaryContainer = GudgudiGoldLight,
    tertiary = GudgudiEmerald,
    onTertiary = GudgudiMaroonDark,
    background = GudgudiMaroonDark,
    onBackground = GudgudiTextPrimary,
    surface = GudgudiMaroonSurface,
    onSurface = GudgudiTextPrimary,
    surfaceVariant = GudgudiMaroonCard,
    onSurfaceVariant = GudgudiTextSecondary,
    outline = GudgudiGoldDark
)

@Composable
fun GudgudiGamingTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GudgudiMaroonDark.toArgb()
            window.navigationBarColor = GudgudiMaroonDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = GudgudiDarkColorScheme,
        typography = Typography,
        content = content
    )
}
