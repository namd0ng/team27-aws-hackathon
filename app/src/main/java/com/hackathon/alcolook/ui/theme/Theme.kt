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
    secondary = CalendarToday,
    onSecondary = CardBackground,
    secondaryContainer = CalendarSelected,
    onSecondaryContainer = TabSelected,
    tertiary = CalendarToday,
    onTertiary = CardBackground,
    tertiaryContainer = CalendarSelected,
    onTertiaryContainer = TabSelected,
    error = DangerSoft,
    onError = CardBackground,
    errorContainer = DangerSoft,
    onErrorContainer = TabSelected,
    outline = TabUnselected,
    background = CardBackground,
    onBackground = TabSelected,
    surface = CardBackground,
    onSurface = TabSelected,
    surfaceVariant = CalendarSelected,
    onSurfaceVariant = TabSelected,
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
            val window = (view.context as android.app.Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
