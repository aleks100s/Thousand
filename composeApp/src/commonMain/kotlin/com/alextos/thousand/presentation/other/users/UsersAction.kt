package com.alextos.thousand.presentation.other.users

import com.alextos.thousand.domain.models.User

sealed interface UsersAction {
    data class StartRenameUser(val user: User) : UsersAction
    data class UpdateEditingUserName(val value: String) : UsersAction
    data object HideRenameUserSheet : UsersAction
    data object SaveEditingUser : UsersAction
    data class DeleteUser(val user: User) : UsersAction
}
