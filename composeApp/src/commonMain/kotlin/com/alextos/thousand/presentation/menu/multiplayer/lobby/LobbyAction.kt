package com.alextos.thousand.presentation.menu.multiplayer.lobby

sealed interface LobbyAction {
    data object LeaveGame: LobbyAction
    data object StartGame: LobbyAction
}
