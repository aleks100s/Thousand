package com.alextos.thousand.presentation.game.game_score

sealed interface GameScoreAction {
    data object LoadGame : GameScoreAction
}
