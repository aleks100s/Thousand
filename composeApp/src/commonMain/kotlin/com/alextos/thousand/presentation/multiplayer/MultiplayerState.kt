package com.alextos.thousand.presentation.multiplayer

data class MultiplayerState(
    val userName: String? = null,
) {
    val isAuthorized: Boolean
        get() = userName.isNullOrBlank().not()
}
