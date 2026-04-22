package com.alextos.thousand.presentation.game.play_game

sealed interface PlayGameAction {
    data object LoadGame : PlayGameAction
    data object RollTheDice: PlayGameAction
    data object FinishTurn: PlayGameAction
    data object FinishRoll: PlayGameAction
}
