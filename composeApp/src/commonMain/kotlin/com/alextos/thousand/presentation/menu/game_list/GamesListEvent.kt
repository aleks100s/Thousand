package com.alextos.thousand.presentation.menu.game_list

sealed interface GamesListEvent {
    data class OpenGame(
        val gameId: Long,
    ) : GamesListEvent
}
