package com.aimobile.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary          = Primary,
    onPrimary        = TextPrimary,
    primaryContainer = Primary.copy(alpha = 0.15f),
    secondary        = Secondary,
    onSecondary      = TextPrimary,
    tertiary         = Accent,
    onTertiary       = TextPrimary,
    background       = Background,
    onBackground     = TextPrimary,
    surface          = CardBg,
    onSurface        = TextPrimary,
    surfaceVariant   = BorderColor,
    onSurfaceVariant = TextSub,
    outline          = BorderColor,
    error            = Danger,
    onError          = TextPrimary
)

@Composable
fun AIMobileTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: transparent status & navigation bars
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
