package com.alextos.thousand.presentation.multiplayer.player_profile

data class PlayerProfileState(
    val username: String = "",
    val isLogoutDialogVisible: Boolean = false,
    val isSignedOut: Boolean = false,
)
