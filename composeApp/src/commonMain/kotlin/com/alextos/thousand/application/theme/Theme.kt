package com.alextos.thousand.application.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    background = Color(0xFF021723),
    onBackground = Color(0xFFF5F0DF),
    surface = Color(0xFF021723),
    onSurface = Color(0xFFF5F0DF),
    surfaceVariant = Color(0xFF173844),
    onSurfaceVariant = Color(0xFFBECFDC),
    surfaceDim = Color(0xFF02131D),
    surfaceBright = Color(0xFF24414D),
    surfaceContainerLowest = Color(0xFF010F18),
    surfaceContainerLow = Color(0xFF071F2B),
    surfaceContainer = Color(0xFF0B2834),
    surfaceContainerHigh = Color(0xFF12323E),
    surfaceContainerHighest = Color(0xFF1B3D49),
    outline = Color(0xFF7897A5),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun ThousandTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}
