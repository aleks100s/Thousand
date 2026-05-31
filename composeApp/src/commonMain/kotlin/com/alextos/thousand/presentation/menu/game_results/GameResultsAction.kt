package com.alextos.thousand.presentation.menu.game_results

sealed interface GameResultsAction {
    data object LoadResults : GameResultsAction
}
