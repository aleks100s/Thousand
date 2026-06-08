package com.alextos.thousand.presentation.menu.multiplayer.create_lobby

sealed interface CreateLobbyEvent {
    data class OpenLobby(val lobbyId: String) :
        CreateLobbyEvent
}
