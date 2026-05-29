package com.alextos.thousand.presentation.multiplayer.multiplayer_game

import com.alextos.thousand.domain.models.RemoteGame

data class MultiplayerGameState(
    val gameCode: String = "",
    val isHost: Boolean = false,
    val error: String? = null,
    val game: RemoteGame? = null,
)
