package com.alextos.thousand.presentation.game.create_game

import com.alextos.thousand.domain.models.User

data class CreateGameState(
    val title: String = "Создание игры",
    val users: List<User> = emptyList(),
    val selectedUsers: Set<User> = emptySet(),
    val createdGameId: Long? = null,
    val newUserName: String = "",
    val isAddUserSheetVisible: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val isVirtualDiceEnabled: Boolean = true,
    val isShakeEnabled: Boolean = true,
    val hasStartLimit: Boolean = true,
    val isBarrel1Active: Boolean = true,
    val isBarrel2Active: Boolean = true,
    val isBarrel3Active: Boolean = false,
    val isTripleBoltFineActive: Boolean = true,
    val isOvertakeFineActive: Boolean = true,
)
