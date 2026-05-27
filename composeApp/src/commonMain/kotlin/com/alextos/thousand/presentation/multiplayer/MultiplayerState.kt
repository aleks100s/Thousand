package com.alextos.thousand.presentation.multiplayer

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Lobby

data class MultiplayerState(
    val isAuthorized: Boolean = false,
    val isLoginSheetVisible: Boolean = false,
    val isJoinLobbySheetVisible: Boolean = false,
    val isLogoutSheetVisible: Boolean = false,
    val email: String = "",
    val password: String = "",
    val lobbyId: String = "",
    val username: String? = null,
    val isLoginInProgress: Boolean = false,
    val isSignUpInProgress: Boolean = false,
    val canLogIn: Boolean = false,
    val canJoinLobby: Boolean = false,
    val error: String? = null,
    val lobbies: List<Lobby> = emptyList(),
    val games: List<Game> = emptyList()
) {
    val loginSheetButtonsEnabled: Boolean
        get() = isLoginInProgress.not() && isSignUpInProgress.not() && canLogIn
}
