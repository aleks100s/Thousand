package com.alextos.thousand.presentation.menu.multiplayer.multiplayer_game

import com.alextos.thousand.domain.usecase.game.server.GameAction

sealed interface MultiplayerGameAction {
    data object DeleteGame :
        MultiplayerGameAction
    data object Rematch :
        MultiplayerGameAction
    data object DismissGameResultSheet :
        MultiplayerGameAction
    data class SendGameAction(val action: GameAction) :
        MultiplayerGameAction
    data class ToggleNotifications(val isEnabled: Boolean) :
        MultiplayerGameAction
}
