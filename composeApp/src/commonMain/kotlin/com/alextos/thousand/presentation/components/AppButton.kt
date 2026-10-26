package com.alextos.thousand.presentation.components

import com.alextos.thousand.application.theme.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class AppButtonStyle { Primary, Secondary }

@Composable
fun AppButton(
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Primary,
    enabled: Boolean = true,
    onClick: () -> Unit,
    secondaryColor: Color = AppColors.White,
    content: @Composable RowScope.() -> Unit,
) {
    val primary = style == AppButtonStyle.Primary
    val background = if (primary) {
        AppBrushes.PrimaryAction
    } else {
        Brush.verticalGradient(listOf(Color.Black, Color.Black))
    }
    Button(
        modifier = modifier.defaultMinSize(minHeight = 48.dp)
            .alpha(if (enabled) 1f else .42f)
            .clip(CircleShape)
            .background(background),
        enabled = enabled,
        onClick = onClick,
        shape = CircleShape,
        border = if (primary) null else BorderStroke(AppDimens.BorderSubtle, secondaryColor),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (primary) Color.Black else secondaryColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = if (primary) Color.Black else secondaryColor,
        ),
    ) {
        ProvideTextStyle(
            MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        ) {
            content()
        }
    }
}
