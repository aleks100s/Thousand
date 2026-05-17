package com.alextos.thousand.presentation.other.users

import com.alextos.thousand.domain.models.User

sealed interface UsersAction {
    data class DeleteUser(val user: User) : UsersAction
}
