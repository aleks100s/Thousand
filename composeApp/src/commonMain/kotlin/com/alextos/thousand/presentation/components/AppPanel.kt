package com.alextos.thousand.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.alextos.thousand.application.theme.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

fun Modifier.appPanel(outline: Color = AppColors.Outline): Modifier =
    clip(AppShapes.Panel).background(AppColors.Surface).border(AppDimens.Border, outline, AppShapes.Panel)

@Composable
fun AppPanel(
    modifier: Modifier = Modifier,
    padding: Dp = AppDimens.CardPadding,
    gap: Dp = AppDimens.ItemGap,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().appPanel().padding(padding),
        verticalArrangement = Arrangement.spacedBy(gap), content = content)
}

@Composable
fun AppIconBadge(
    icon: DrawableResource,
    modifier: Modifier = Modifier,
    large: Boolean = false,
    compact: Boolean = false,
    tint: Color = AppColors.Highlight,
) {
    val size = if (large) AppDimens.BadgeLarge else if (compact) AppDimens.BadgeCompact else AppDimens.Badge
    Box(modifier.size(size).clip(AppShapes.Row).background(AppBrushes.Icon)
        .border(AppDimens.BorderSubtle, AppColors.Outline, AppShapes.Row), contentAlignment = Alignment.Center) {
        Icon(painterResource(icon), contentDescription = null,
            modifier = Modifier.size(if (large) AppDimens.BadgeLargeIcon else AppDimens.BadgeIcon), tint = tint)
    }
}
