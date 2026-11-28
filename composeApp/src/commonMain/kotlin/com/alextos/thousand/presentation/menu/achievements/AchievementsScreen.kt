package com.alextos.thousand.presentation.menu.achievements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alextos.thousand.common.Screen

@Composable
fun AchievementsScreen(onGoBack: () -> Unit) {
    Screen(
        modifier = Modifier,
        title = "Достижения",
        goBack = onGoBack,
    ) {}
}
