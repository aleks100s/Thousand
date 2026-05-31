package com.alextos.thousand.presentation.menu.play_game

import com.alextos.thousand.domain.usecase.game.server.GameAction
import com.alextos.thousand.domain.models.Die

sealed interface PlayGameAction {
    data class SendGameAction(val action: GameAction): PlayGameAction
    data object FinishGame: PlayGameAction
    data class ApplyDiceRoll(val dice: List<Die>): PlayGameAction
    data class SetNotificationEnabled(val isEnabled: Boolean) : PlayGameAction
}
