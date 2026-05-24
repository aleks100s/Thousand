package com.alextos.thousand.presentation.multiplayer

import kotlinx.serialization.Serializable

sealed interface MultiplayerRoute {
    @Serializable
    data object Multiplayer : MultiplayerRoute

    @Serializable
    data object CreateLobby : MultiplayerRoute

    @Serializable
    data class Lobby(val lobbyId: String) : MultiplayerRoute

    @Serializable
    data class MultiplayerGame(val lobbyId: String) : MultiplayerRoute
}
