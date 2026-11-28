package com.alextos.thousand.presentation.menu.multiplayer.player_profile

import com.alextos.thousand.domain.models.User

data class PlayerProfileState(
    val user: User? = null,
    val isDeleteAccountDialogVisible: Boolean = false,
    val isDeleteInProgress: Boolean = false,
    val deleteError: String? = null,
)
