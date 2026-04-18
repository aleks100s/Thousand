package com.alextos.thousand.presentation.game.create_game

import com.alextos.thousand.domain.models.User

data class CreateGameState(
    val title: String = "Создание игры",
    val users: List<User> = emptyList(),
    val selectedUsers: List<User> = emptyList(),
    val newUserName: String = "",
    val isAddUserSheetVisible: Boolean = false
)
