package com.alextos.thousand.presentation.menu.multiplayer

import com.alextos.thousand.domain.models.Lobby
import com.alextos.thousand.domain.models.RemoteGame

data class MultiplayerState(
    val isAuthorized: Boolean = false,
    val isConnecting: Boolean = false,
    val connectionError: String? = null,
    val isJoinLobbySheetVisible: Boolean = false,
    val isJoiningLobby: Boolean = false,
    val lobbyId: String = "",
    val lobbyError: String? = null,
    val lobbies: List<Lobby> = emptyList(),
    val activeGames: List<RemoteGame> = emptyList(),
    val finishedGames: List<RemoteGame> = emptyList(),
)
