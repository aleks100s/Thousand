package com.alextos.thousand.presentation.menu.multiplayer.player_profile

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.RemoteUserInfo

data class PlayerProfileState(
    val userId: String = "",
    val username: String = "",
    val user: User? = null,
    val userInfo: RemoteUserInfo? = null,
    val isLogoutDialogVisible: Boolean = false,
    val isDeleteAccountDialogVisible: Boolean = false,
    val isDeleteInProgress: Boolean = false,
)
