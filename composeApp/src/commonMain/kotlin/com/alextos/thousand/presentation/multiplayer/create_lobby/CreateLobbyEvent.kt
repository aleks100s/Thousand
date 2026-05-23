package com.alextos.thousand.presentation.multiplayer.create_lobby

sealed interface CreateLobbyEvent {
    data class OpenLobby(val lobbyId: String) : CreateLobbyEvent
}
