package com.alextos.thousand.presentation.game.game_list

sealed interface GamesListAction {
    data object LoadGames : GamesListAction
}
