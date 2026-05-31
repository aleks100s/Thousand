package com.alextos.thousand.presentation.menu.game_list

sealed interface GamesListAction {
    data object LoadGames : GamesListAction
    data class DeleteGame(val gameId: Long) : GamesListAction
    data class CreateRematch(val gameId: Long) : GamesListAction
}
