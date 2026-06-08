package com.alextos.thousand.presentation.menu.multiplayer

sealed interface MultiplayerEvent {
    data class OpenLobby(val lobbyId: String) :
        MultiplayerEvent
}
