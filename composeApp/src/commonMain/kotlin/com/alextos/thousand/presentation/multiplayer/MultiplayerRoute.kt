package com.alextos.thousand.presentation.multiplayer

import kotlinx.serialization.Serializable

sealed interface MultiplayerRoute {
    @Serializable
    data object Multiplayer : MultiplayerRoute

    @Serializable
    data object CreateLobby : MultiplayerRoute
}
