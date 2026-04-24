package com.alextos.thousand.presentation.game.create_game

sealed interface CreateGameEvent {
    data class OpenGame(
        val gameId: Long,
    ) : CreateGameEvent
}
