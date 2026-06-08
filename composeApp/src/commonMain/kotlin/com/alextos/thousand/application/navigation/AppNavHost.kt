package com.alextos.thousand.application.navigation

import androidx.compose.runtime.Composable
import com.alextos.thousand.presentation.menu.MenuRoot

@Composable
fun AppNavHost(
    hideMultiplayer: Boolean,
) {
    MenuRoot(hideMultiplayer = hideMultiplayer)
}
