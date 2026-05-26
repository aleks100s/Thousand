package com.alextos.thousand.presentation.multiplayer.lobby

sealed interface LobbyAction {
    data object LeaveGame: LobbyAction
    data object StartGame: LobbyAction
}
