package com.alextos.thousand.presentation.multiplayer.lobby

import com.alextos.thousand.domain.models.GameSettings

data class LobbyState(
    val lobbyId: String = "",
    val gameSettings: GameSettings = GameSettings(),
    val isHost: Boolean = false
) {
    val isStartButtonEnabled: Boolean = isHost && gameSettings.players.count() > 1
}
