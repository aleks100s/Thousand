package com.alextos.thousand.presentation.menu.users

import com.alextos.thousand.domain.models.User

sealed interface UsersAction {
    data object StartAddUser : UsersAction
    data class StartRenameUser(val user: User) : UsersAction
    data class UpdateEditingUserName(val value: String) : UsersAction
    data object HideRenameUserSheet : UsersAction
    data object SaveEditingUser : UsersAction
    data class DeleteUser(val user: User) : UsersAction
    data class RequestDeleteUser(val user: User) : UsersAction
    data object CancelDeleteUser : UsersAction
}
