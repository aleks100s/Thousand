package com.alextos.thousand.presentation.menu.multiplayer.multiplayer_game

import com.alextos.thousand.domain.models.RemoteUserInfo
import com.alextos.thousand.domain.usecase.game.server.GameState

data class MultiplayerGameState(
    val gameCode: String = "",
    val isHost: Boolean = false,
    val error: String? = null,
    val gameState: GameState = GameState(),
    val isNotificationEnabled: Boolean = true,
    val usersInfo: Map<String, RemoteUserInfo> = emptyMap(),
    val gameResultSheet: MultiplayerGameResultSheetUi? = null,
)

data class MultiplayerGameResultSheetUi(
    val winnerName: String,
    val isCurrentUser: Boolean,
)
