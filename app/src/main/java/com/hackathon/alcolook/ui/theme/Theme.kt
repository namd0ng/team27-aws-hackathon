package com.hackathon.alcolook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TabSelected,
    onPrimary = CardBackground,
    primaryContainer = CalendarSelected,
    onPrimaryContainer = TabSelected,
    secondary = TextSecondary,
    onSecondary = CardBackground,
    secondaryContainer = AppBackground,
    onSecondaryContainer = TextPrimary,
    tertiary = StatusWarning,
    onTertiary = CardBackground,
    tertiaryContainer = WarningSoft,
    onTertiaryContainer = TextPrimary,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = AppBackground,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    outlineVariant = TextTertiary
)

@Composable
fun AlcoLookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as androidx.activity.ComponentActivity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}