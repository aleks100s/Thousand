package com.alextos.thousand.presentation.menu.multiplayer.create_lobby

import com.alextos.thousand.domain.models.GameSettings

sealed interface CreateLobbyAction {
    data class UpdateGameSettings(val settings: GameSettings) :
        CreateLobbyAction
    data object OpenLobby :
        CreateLobbyAction
}
