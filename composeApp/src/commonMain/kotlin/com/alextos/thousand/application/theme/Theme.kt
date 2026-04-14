package com.alextos.thousand.application.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1E5EFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = Color(0xFF00174A),
    secondary = Color(0xFF006782),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFBCE9FF),
    onSecondaryContainer = Color(0xFF001F29),
    tertiary = Color(0xFF6A5778),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF2DAFF),
    onTertiaryContainer = Color(0xFF241432),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF181C22),
    surface = Color(0xFFF7F9FC),
    onSurface = Color(0xFF181C22),
    surfaceVariant = Color(0xFFDEE3EB),
    onSurfaceVariant = Color(0xFF424A53),
    outline = Color(0xFF727A84),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB4C5FF),
    onPrimary = Color(0xFF002B78),
    primaryContainer = Color(0xFF0040AD),
    onPrimaryContainer = Color(0xFFDCE7FF),
    secondary = Color(0xFF69D3FF),
    onSecondary = Color(0xFF003547),
    secondaryContainer = Color(0xFF004D66),
    onSecondaryContainer = Color(0xFFBCE9FF),
    tertiary = Color(0xFFD6BEE4),
    onTertiary = Color(0xFF3B2948),
    tertiaryContainer = Color(0xFF523F5F),
    onTertiaryContainer = Color(0xFFF2DAFF),
    background = Color(0xFF101419),
    onBackground = Color(0xFFE1E6EE),
    surface = Color(0xFF101419),
    onSurface = Color(0xFFE1E6EE),
    surfaceVariant = Color(0xFF424A53),
    onSurfaceVariant = Color(0xFFC2C8D0),
    outline = Color(0xFF8C939D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun ThousandTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
