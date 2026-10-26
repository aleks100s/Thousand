package com.alextos.thousand.presentation.menu.create_game

import com.alextos.thousand.domain.models.PlayerAvatar
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.models.GameSettings

data class CreateGameState(
    val isLoadingUsers: Boolean = true,
    val users: List<User> = emptyList(),
    val selectedUsers: Set<User> = emptySet(),
    val step: CreateGameStep = CreateGameStep.Players,
    val isEditingPlayers: Boolean = false,
    val isPlayerEditorVisible: Boolean = false,
    val editingUser: User? = null,
    val playerName: String = "",
    val playerAvatar: PlayerAvatar = PlayerAvatar.Neutral,
    val playerKind: UserKind = UserKind.LocalUser,
    val isPlayerOperationRunning: Boolean = false,
    val playerError: String? = null,
    val pendingDeleteUser: User? = null,
    val gameSettings: GameSettings = GameSettings(),
) {
    val title: String
        get() = when (step) {
            CreateGameStep.Players -> when {
                isEditingPlayers -> "Редактировать"
                selectedUsers.isEmpty() -> "Выбор игроков"
                else -> selectedPlayersLabel(selectedUsers.size)
            }
            CreateGameStep.Settings -> "Настройки игры"
        }

    val playerNameError: String?
        get() {
            val normalizedName = playerName.trim()
            if (normalizedName.isBlank()) return null
            return if (users.any { it.id != editingUser?.id && it.name.trim().equals(normalizedName, ignoreCase = true) }) {
                "Пользователь с таким именем уже существует"
            } else null
        }

    val canSavePlayer: Boolean
        get() = isPlayerEditorVisible && !isPlayerOperationRunning && playerName.trim().isNotBlank() && playerNameError == null
}

enum class CreateGameStep { Players, Settings }

internal fun selectedPlayersLabel(count: Int): String = when {
    count % 100 in 11..14 -> "Выбрано $count игроков"
    count % 10 == 1 -> "Выбран $count игрок"
    count % 10 in 2..4 -> "Выбрано $count игрока"
    else -> "Выбрано $count игроков"
}
