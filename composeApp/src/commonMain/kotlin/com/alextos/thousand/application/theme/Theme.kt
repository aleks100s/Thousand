package com.alextos.thousand.application.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Accent,
    onPrimary = AppColors.OnAccent,
    primaryContainer = AppColors.SurfaceRaised,
    onPrimaryContainer = AppColors.TextPrimary,
    secondary = AppColors.Positive,
    onSecondary = AppColors.OnAccent,
    secondaryContainer = AppColors.SurfaceRaised,
    onSecondaryContainer = AppColors.TextPrimary,
    tertiary = AppColors.Highlight,
    onTertiary = AppColors.OnAccent,
    tertiaryContainer = AppColors.SurfaceRaised,
    onTertiaryContainer = AppColors.TextPrimary,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Background,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceMuted,
    onSurfaceVariant = AppColors.TextSecondary,
    surfaceDim = AppColors.Background,
    surfaceBright = AppColors.SurfaceRaised,
    surfaceContainerLowest = AppColors.Background,
    surfaceContainerLow = AppColors.Sheet,
    surfaceContainer = AppColors.SurfaceInset,
    surfaceContainerHigh = AppColors.SurfaceRaised,
    surfaceContainerHighest = AppColors.SurfaceMuted,
    outline = AppColors.Outline,
    outlineVariant = AppColors.OutlineSubtle,
    error = AppColors.Danger,
    onError = AppColors.OnAccent,
)

@Composable
fun ThousandTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        shapes = androidx.compose.material3.Shapes(
            extraSmall = AppShapes.Inset,
            small = AppShapes.Row,
            medium = AppShapes.Row,
            large = AppShapes.Panel,
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(AppDimens.SheetRadius),
        ),
        content = content,
    )
}
