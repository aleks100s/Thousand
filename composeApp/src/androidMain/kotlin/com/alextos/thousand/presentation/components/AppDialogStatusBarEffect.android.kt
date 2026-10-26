package com.alextos.thousand.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
internal actual fun AppDialogStatusBarEffect() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view as? DialogWindowProvider)?.window
            ?: generateSequence(view.parent) { it.parent }
                .filterIsInstance<DialogWindowProvider>().firstOrNull()?.window
        window?.let {
            WindowCompat.getInsetsController(it, view).apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.statusBars())
            }
        }
        // The window is being dismissed; showing the bar here would cause a flash.
        onDispose {}
    }
}
