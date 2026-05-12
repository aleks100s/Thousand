package com.alextos.thousand.presentation.game.game_results

sealed interface GameResultsAction {
    data object LoadResults : GameResultsAction
}
