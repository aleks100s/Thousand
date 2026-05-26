package com.alextos.thousand.presentation.multiplayer.lobby

sealed interface LobbyEvent {
    data object Disconnect: LobbyEvent
    data class StartGame(val gameID: String): LobbyEvent
}