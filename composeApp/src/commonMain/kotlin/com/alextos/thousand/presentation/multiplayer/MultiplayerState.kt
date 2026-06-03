package com.alextos.thousand.presentation.multiplayer

import com.alextos.thousand.domain.models.Lobby
import com.alextos.thousand.domain.models.RemoteGame

data class MultiplayerState(
    val isAuthorized: Boolean = false,
    val isLoginSheetVisible: Boolean = false,
    val isJoinLobbySheetVisible: Boolean = false,
    val isLogoutSheetVisible: Boolean = false,
    val email: String = "",
    val password: String = "",
    val lobbyId: String = "",
    val currentUserId: String? = null,
    val username: String? = null,
    val isLoginInProgress: Boolean = false,
    val isSignUpInProgress: Boolean = false,
    val canLogIn: Boolean = false,
    val canJoinLobby: Boolean = false,
    val error: String? = null,
    val lobbies: List<Lobby> = emptyList(),
    val activeGames: List<RemoteGame> = emptyList(),
    val finishedGames: List<RemoteGame> = emptyList(),
) {
    val loginSheetButtonsEnabled: Boolean
        get() = isLoginInProgress.not() && isSignUpInProgress.not() && canLogIn

    val hasHostedActiveGameOrLobby: Boolean
        get() = currentUserId?.let { userId ->
            activeGames.any { game -> game.host == userId } ||
                lobbies.any { lobby -> lobby.host == userId }
        } ?: false
}
