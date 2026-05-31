package com.alextos.thousand.presentation.menu.create_game

sealed interface CreateGameEvent {
    data class OpenGame(
        val gameId: Long,
    ) : CreateGameEvent
}
