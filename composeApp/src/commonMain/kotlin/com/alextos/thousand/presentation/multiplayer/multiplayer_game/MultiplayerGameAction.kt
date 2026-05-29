package com.alextos.thousand.presentation.multiplayer.multiplayer_game

import com.alextos.thousand.domain.usecase.game.server.GameAction

sealed interface MultiplayerGameAction {
    data object DeleteGame : MultiplayerGameAction
    data class SendGameAction(val action: GameAction) : MultiplayerGameAction
}
