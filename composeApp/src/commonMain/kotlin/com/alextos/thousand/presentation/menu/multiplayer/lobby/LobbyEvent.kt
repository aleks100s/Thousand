package com.alextos.thousand.presentation.menu.multiplayer.lobby

sealed interface LobbyEvent {
    data object Disconnect: LobbyEvent
    data class StartGame(val gameID: String):
        LobbyEvent
}