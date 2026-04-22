package com.alextos.thousand.screens.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alextos.thousand.common.Screen

@Composable
fun EmptyScreen(
    title: String,
    goBack: (() -> Unit)? = null,
) {
    Screen(
        modifier = Modifier,
        title = title,
        goBack = goBack,
    ) {
        Box(
            modifier = it.fillMaxSize(),
        )
    }
}
