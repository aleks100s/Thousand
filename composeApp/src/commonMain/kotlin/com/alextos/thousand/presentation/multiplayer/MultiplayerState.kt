package com.alextos.thousand.presentation.multiplayer

data class MultiplayerState(
    val isAuthorized: Boolean = false,
    val isLoginSheetVisible: Boolean = false,
    val isJoinLobbySheetVisible: Boolean = false,
    val email: String = "",
    val password: String = "",
    val lobbyId: String = "",
    val isLoginInProgress: Boolean = false,
    val isSignUpInProgress: Boolean = false,
    val canLogIn: Boolean = false,
    val canJoinLobby: Boolean = false,
    val error: String? = null
)
