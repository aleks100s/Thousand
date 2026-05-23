package com.alextos.thousand.presentation.multiplayer.create_lobby

import com.alextos.thousand.presentation.game.components.GameSettings

sealed interface CreateLobbyAction {
    data class UpdateGameSettings(val settings: GameSettings) : CreateLobbyAction
    data object OpenLobby : CreateLobbyAction
}
