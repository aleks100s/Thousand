package com.alextos.thousand.presentation.game.play_game

import com.alextos.thousand.domain.models.Die

sealed interface PlayGameAction {
    data object LoadGame : PlayGameAction
    data object RollTheDice: PlayGameAction
    data object FinishTurn: PlayGameAction
    data object FinishRoll: PlayGameAction
    data class ApplyDiceRoll(val dice: List<Die>): PlayGameAction
}
