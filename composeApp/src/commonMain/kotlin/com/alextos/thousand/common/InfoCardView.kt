package com.alextos.thousand.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.alextos.thousand.application.theme.*
import com.alextos.thousand.presentation.components.AppPanel
import com.alextos.thousand.presentation.components.AppIconBadge
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun InfoCardView(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    title: String,
    text: String,
    bottomContent: @Composable ColumnScope.() -> Unit = {},
) {
    AppPanel(modifier) {
        AppIconBadge(icon, large = true)
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
        Text(text, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary)
        bottomContent()
    }
}
