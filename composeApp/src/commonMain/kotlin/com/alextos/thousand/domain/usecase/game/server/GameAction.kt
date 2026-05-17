package com.alextos.thousand.domain.usecase.game.server

import com.alextos.thousand.domain.models.Die

sealed interface GameAction {
    data object RollDice: GameAction
    data class ApplyRoll(val dice: List<Die>): GameAction
    data object FinishRoll: GameAction
    data object FinishTurn: GameAction
    data object BotTurn: GameAction
}