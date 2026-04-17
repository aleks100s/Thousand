package com.alextos.thousand.presentation.game.create_game

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alextos.thousand.common.Screen
import com.alextos.thousand.screens.common.EmptyScreen

@Composable
fun CreateGameScreen(goBack: () -> Unit) {
    Screen(
        modifier = Modifier,
        title = "Создание игры",
        goBack = goBack
    ) {

    }
}
