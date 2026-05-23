package com.alextos.thousand.presentation.multiplayer

data class MultiplayerState(
    val isAuthorized: Boolean = false,
    val isLoginSheetVisible: Boolean = false,
    val email: String = "",
    val password: String = "",
    val isLoginInProgress: Boolean = false,
    val isSignUpInProgress: Boolean = false,
    val canLogIn: Boolean = false,
    val error: String? = null
)
