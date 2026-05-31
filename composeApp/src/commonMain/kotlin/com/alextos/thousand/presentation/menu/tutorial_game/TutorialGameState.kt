package com.alextos.thousand.presentation.menu.tutorial_game

import com.alextos.thousand.domain.usecase.game.server.GameState

data class TutorialGameState(
    val isLoading: Boolean = false,
    val gameState: GameState = GameState(),
    val messageToShow: String? = null
) {
    val title: String = "Обучение"
}
