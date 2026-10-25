package com.alextos.thousand.presentation.components

import com.alextos.thousand.application.theme.AppDimens
import com.alextos.thousand.application.theme.AppColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Independently lazy content with a continuous card background and outer border. */
@Composable
internal fun PanelSegment(
    first: Boolean,
    last: Boolean,
    padding: Dp = AppDimens.CardPadding,
    gap: Dp = 8.dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(
        topStart = if (first) AppDimens.PanelRadius else 0.dp, topEnd = if (first) AppDimens.PanelRadius else 0.dp,
        bottomStart = if (last) AppDimens.PanelRadius else 0.dp, bottomEnd = if (last) AppDimens.PanelRadius else 0.dp,
    )
    Column(
        modifier.widthIn(max = 760.dp).fillMaxWidth().clip(shape).background(AppColors.Surface)
            .drawWithCache {
                val stroke = AppDimens.Border.toPx()
                val inset = stroke / 2
                val right = size.width - inset
                val bottom = size.height - inset
                val radius = AppDimens.PanelRadius.toPx().coerceAtMost(size.height).coerceAtMost(size.width / 2)
                val path = Path().apply {
                    if (first) {
                        moveTo(inset, radius)
                        arcTo(Rect(inset, inset, 2 * radius - inset, 2 * radius - inset), 180f, 90f, false)
                        lineTo(size.width - radius, inset)
                        arcTo(Rect(size.width - 2 * radius + inset, inset, right, 2 * radius - inset), 270f, 90f, false)
                    } else moveTo(right, 0f)
                    lineTo(right, if (last) size.height - radius else size.height)
                    if (last) {
                        arcTo(Rect(size.width - 2 * radius + inset, size.height - 2 * radius + inset, right, bottom), 0f, 90f, false)
                        lineTo(radius, bottom)
                        arcTo(Rect(inset, size.height - 2 * radius + inset, 2 * radius - inset, bottom), 90f, 90f, false)
                    } else moveTo(inset, size.height)
                    lineTo(inset, if (first) radius else 0f)
                }
                onDrawBehind { drawPath(path, AppColors.Outline, style = Stroke(stroke)) }
            }
            .padding(start = padding, end = padding, top = if (first) padding else 0.dp, bottom = if (last) padding else gap),
        verticalArrangement = Arrangement.spacedBy(gap),
        content = content,
    )
}
