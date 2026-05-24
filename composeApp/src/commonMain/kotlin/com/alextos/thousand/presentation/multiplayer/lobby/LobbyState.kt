package com.alextos.thousand.presentation.multiplayer.lobby

import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.UserProfile

data class LobbyState(
    val lobbyId: String = "",
    val gameSettings: GameSettings = GameSettings(),
    val players: List<UserProfile> = emptyList(),
    val isHost: Boolean = false,
    val error: String? = null
) {
    val isStartButtonEnabled: Boolean = isHost && players.count() > 1
}
