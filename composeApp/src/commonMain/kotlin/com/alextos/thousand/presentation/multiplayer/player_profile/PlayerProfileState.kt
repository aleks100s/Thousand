package com.alextos.thousand.presentation.multiplayer.player_profile

data class PlayerProfileState(
    val userId: String = "",
    val username: String = "",
    val isLogoutDialogVisible: Boolean = false,
    val isDeleteInProgress: Boolean = false,
)
