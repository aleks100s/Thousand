package com.alextos.thousand.presentation.multiplayer

sealed interface MultiplayerEvent {
    data class OpenLobby(val lobbyId: String) : MultiplayerEvent
}
