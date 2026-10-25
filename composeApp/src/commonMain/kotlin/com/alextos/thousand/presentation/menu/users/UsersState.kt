package com.alextos.thousand.presentation.menu.users

import com.alextos.thousand.domain.models.User

data class UsersState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
    val editingUser: User? = null,
    val editingUserName: String = "",
    val isAddingUser: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val pendingDeleteUser: User? = null,
    val statistics: Map<String, UserStatisticsUi> = emptyMap(),
) {
    val isEditorVisible: Boolean get() = isAddingUser || editingUser != null

    val editingUserNameError: String?
        get() {
            val normalizedName = editingUserName.trim()
            if (normalizedName.isBlank()) return null
            val isNameAlreadyUsed = users.any {
                it.id != editingUser?.id && it.name.trim().equals(normalizedName, ignoreCase = true)
            }
            return if (isNameAlreadyUsed) {
                "Пользователь с таким именем уже существует"
            } else {
                null
            }
        }

    val canSaveEditingUser: Boolean
        get() = isEditorVisible && !isSaving &&
            editingUserName.trim().isNotBlank() &&
            editingUserNameError == null
}

data class UserStatisticsUi(val wins: Int = 0, val games: Int = 0)
