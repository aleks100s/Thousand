package com.alextos.thousand.presentation.menu.game_score

import com.alextos.thousand.presentation.components.AppPanel
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun HistoryPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    AppPanel(modifier.widthIn(max = 760.dp), gap = 8.dp, content = content)
}
