package com.alextos.thousand.presentation.menu.multiplayer.lobby

import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.User

data class LobbyState(
    val lobbyId: String = "",
    val gameSettings: GameSettings = GameSettings(),
    val players: List<User> = emptyList(),
    val isHost: Boolean = false,
    val error: String? = null
) {
    val isStartButtonEnabled: Boolean = isHost && players.count() > 1
}
