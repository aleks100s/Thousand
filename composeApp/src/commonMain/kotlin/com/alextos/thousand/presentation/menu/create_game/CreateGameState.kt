package com.alextos.thousand.presentation.menu.create_game

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.models.GameSettings

data class CreateGameState(
    val users: List<User> = emptyList(),
    val selectedUsers: Set<User> = emptySet(),
    val step: CreateGameStep = CreateGameStep.Players,
    val newUserName: String = "",
    val newUserKind: UserKind = UserKind.LocalUser,
    val isAddUserSheetVisible: Boolean = false,
    val gameSettings: GameSettings = GameSettings(),
) {
    val title: String
        get() = when (step) {
            CreateGameStep.Players -> "Выбор игроков"
            CreateGameStep.Settings -> "Настройки игры"
        }

    val newUserNameError: String?
        get() {
            val normalizedName = newUserName.trim()
            if (normalizedName.isBlank()) return null
            val isNameAlreadyUsed = users.any { user ->
                user.name.trim().equals(normalizedName, ignoreCase = true)
            }
            return if (isNameAlreadyUsed) {
                "Пользователь с таким именем уже существует"
            } else {
                null
            }
        }

    val canSaveNewUser: Boolean
        get() = newUserName.trim().isNotBlank() && newUserNameError == null
}

enum class CreateGameStep {
    Players,
    Settings,
}
