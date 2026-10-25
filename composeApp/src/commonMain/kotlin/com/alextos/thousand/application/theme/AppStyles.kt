package com.alextos.thousand.application.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Brush

object AppShapes {
    val Panel = RoundedCornerShape(AppDimens.PanelRadius)
    val Row = RoundedCornerShape(AppDimens.RowRadius)
    val Inset = RoundedCornerShape(AppDimens.InsetRadius)
    val Sheet = RoundedCornerShape(topStart = AppDimens.SheetRadius, topEnd = AppDimens.SheetRadius)
}

object AppBrushes {
    val Panel = SolidColor(AppColors.Surface)
    val Icon = Brush.verticalGradient(listOf(AppColors.IconSurfaceTop, AppColors.IconSurfaceBottom))
    val PrimaryAction = Brush.verticalGradient(listOf(AppColors.ActionTop, AppColors.ActionBottom))
    val Navigation = Brush.verticalGradient(listOf(AppColors.NavigationTop, AppColors.NavigationBottom))
}
