package com.alextos.thousand.presentation.multiplayer.lobby

sealed interface LobbyEvent {
    data object Disconnect: LobbyEvent
}