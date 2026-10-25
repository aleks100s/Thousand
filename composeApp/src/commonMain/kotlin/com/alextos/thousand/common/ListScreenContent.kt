package com.alextos.thousand.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Keeps the screen background full width while centering landscape lists. */
@Composable
fun ListScreenContent(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val contentWidth = if (maxWidth > maxHeight) 600.dp else Dp.Infinity
        Box(
            Modifier.align(Alignment.TopCenter).widthIn(max = contentWidth).fillMaxSize(),
        ) {
            content()
        }
    }
}
