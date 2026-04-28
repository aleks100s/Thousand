package com.alextos.thousand.presentation.game.play_game

import com.alextos.thousand.domain.game.server.GameState

data class PlayGameState(
    val isLoading: Boolean = true,
    val isManualInputEnabled: Boolean = false,
    val gameState: GameState = GameState()
) {
    val title: String
        get() = gameState.game?.let { "Игра №${it.id}" } ?: ""
}
