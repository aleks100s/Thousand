package com.alextos.thousand.common

import androidx.compose.foundation.layout.size
import com.alextos.thousand.application.theme.AppDimens
import com.alextos.thousand.application.theme.AppBrushes
import com.alextos.thousand.application.theme.AppColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Shared navigation chrome; screen content and menus retain their own theme. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppNavigationBar(
    title: String,
    titleView: @Composable (() -> Unit)?,
    goBack: (() -> Unit)?,
    backButtonIcon: Painter,
    actions: @Composable (Color) -> @Composable RowScope.() -> Unit,
) {
    val foreground = AppColors.TextPrimary
    BoxWithConstraints {
        val compact = maxWidth > maxHeight && maxHeight < 600.dp
        CenterAlignedTopAppBar(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(AppBrushes.Navigation)
                .drawWithContent {
                    drawContent()
                    drawLine(AppColors.NavigationDivider, Offset(0f, size.height - .5.dp.toPx()), Offset(size.width, size.height - .5.dp.toPx()), 1.dp.toPx())
                    drawLine(foreground.copy(alpha = .16f), Offset(12.dp.toPx(), .5.dp.toPx()), Offset(size.width - 12.dp.toPx(), .5.dp.toPx()), 1.dp.toPx())
                },
            title = titleView ?: {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            expandedHeight = if (compact) 48.dp else 64.dp,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                navigationIconContentColor = foreground,
                titleContentColor = foreground,
                actionIconContentColor = foreground,
            ),
            navigationIcon = {
                if (goBack != null) {
                    IconButton(onClick = goBack) {
                        Icon(backButtonIcon, contentDescription = "Назад", modifier = Modifier.size(AppDimens.Icon))
                    }
                }
            },
            actions = actions(foreground),
        )
    }
}
