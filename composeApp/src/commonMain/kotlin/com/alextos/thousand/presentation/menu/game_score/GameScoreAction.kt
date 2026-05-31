package com.alextos.thousand.presentation.menu.game_score

sealed interface GameScoreAction {
    data object LoadGame : GameScoreAction
}
