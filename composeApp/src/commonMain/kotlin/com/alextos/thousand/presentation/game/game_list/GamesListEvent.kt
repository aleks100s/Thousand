package com.alextos.thousand.presentation.game.game_list

sealed interface GamesListEvent {
    data class OpenGame(
        val gameId: Long,
    ) : GamesListEvent
}
