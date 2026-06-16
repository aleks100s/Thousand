package com.alextos.thousand.presentation.menu.multiplayer

import com.alextos.thousand.domain.models.Lobby
import com.alextos.thousand.domain.models.RemoteGame

data class MultiplayerState(
    val isAuthorized: Boolean = false,
    val isLoginSheetVisible: Boolean = false,
    val isJoinLobbySheetVisible: Boolean = false,
    val lobbyId: String = "",
    val currentUserId: String? = null,
    val username: String? = null,
    val isLoginInProgress: Boolean = false,
    val isSignUpInProgress: Boolean = false,
    val canJoinLobby: Boolean = false,
    val loginError: String? = null,
    val lobbyError: String? = null,
    val lobbies: List<Lobby> = emptyList(),
    val activeGames: List<RemoteGame> = emptyList(),
    val finishedGames: List<RemoteGame> = emptyList(),
) {
    val hasActiveGameOrLobby: Boolean = activeGames.isNotEmpty() || lobbies.isNotEmpty()
}
