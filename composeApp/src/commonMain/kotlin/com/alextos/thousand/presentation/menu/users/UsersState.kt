package com.alextos.thousand.presentation.menu.users

import com.alextos.thousand.domain.models.User

data class UsersState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
    val editingUser: User? = null,
    val editingUserName: String = "",
) {
    val editingUserNameError: String?
        get() {
            val user = editingUser ?: return null
            val normalizedName = editingUserName.trim()
            if (normalizedName.isBlank()) return null
            val isNameAlreadyUsed = users.any {
                it.id != user.id && it.name.trim().equals(normalizedName, ignoreCase = true)
            }
            return if (isNameAlreadyUsed) {
                "Пользователь с таким именем уже существует"
            } else {
                null
            }
        }

    val canSaveEditingUser: Boolean
        get() = editingUser != null &&
            editingUserName.trim().isNotBlank() &&
            editingUserNameError == null
}
