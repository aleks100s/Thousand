package com.alextos.thousand.domain.game.server

import com.alextos.thousand.domain.models.Die

sealed interface GameAction {
    data object RollDice: GameAction
    data class ApplyRoll(val dice: List<Die>): GameAction
    data object FinishRoll: GameAction
    data object FinishTurn: GameAction
}