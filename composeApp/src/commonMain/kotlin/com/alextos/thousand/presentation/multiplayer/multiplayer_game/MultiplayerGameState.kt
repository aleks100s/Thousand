package com.alextos.thousand.presentation.multiplayer.multiplayer_game

import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.usecase.game.server.GameState

data class MultiplayerGameState(
    val gameCode: String = "",
    val isHost: Boolean = false,
    val error: String? = null,
    val gameState: GameState = GameState(),
    val isNotificationEnabled: Boolean = true
)
