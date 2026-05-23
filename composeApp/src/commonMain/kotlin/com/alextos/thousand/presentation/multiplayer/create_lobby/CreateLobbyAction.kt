package com.alextos.thousand.presentation.multiplayer.create_lobby

import com.alextos.thousand.domain.models.GameSettings

sealed interface CreateLobbyAction {
    data class UpdateGameSettings(val settings: GameSettings) : CreateLobbyAction
    data object OpenLobby : CreateLobbyAction
}
