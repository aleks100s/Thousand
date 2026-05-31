package com.alextos.thousand.presentation.menu.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.usecase.user.DeleteUserUseCase
import com.alextos.thousand.domain.usecase.user.GetAllUsersUseCase
import com.alextos.thousand.domain.usecase.user.UpdateUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsersViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(UsersState())
    val state: StateFlow<UsersState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAllUsersUseCase().collect { users ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        users = users,
                    )
                }
            }
        }
    }

    fun onAction(action: UsersAction) {
        when (action) {
            is UsersAction.StartRenameUser -> startRenameUser(action.user)
            is UsersAction.UpdateEditingUserName -> updateEditingUserName(action.value)
            UsersAction.HideRenameUserSheet -> hideRenameUserSheet()
            UsersAction.SaveEditingUser -> saveEditingUser()
            is UsersAction.DeleteUser -> deleteUser(action.user)
        }
    }

    private fun startRenameUser(user: User) {
        _state.update {
            it.copy(
                editingUser = user,
                editingUserName = user.name,
            )
        }
    }

    private fun updateEditingUserName(value: String) {
        _state.update {
            it.copy(editingUserName = value)
        }
    }

    private fun hideRenameUserSheet() {
        _state.update {
            it.copy(
                editingUser = null,
                editingUserName = "",
            )
        }
    }

    private fun saveEditingUser() {
        val currentState = state.value
        val user = currentState.editingUser ?: return
        if (currentState.canSaveEditingUser.not()) return

        viewModelScope.launch {
            val name = currentState.editingUserName.trim()
            updateUserUseCase(user.copy(name = name))
            hideRenameUserSheet()
        }
    }

    private fun deleteUser(user: User) {
        if (user.kind == UserKind.MainUser) return

        viewModelScope.launch {
            deleteUserUseCase(user.id)
            if (state.value.editingUser?.id == user.id) {
                hideRenameUserSheet()
            }
        }
    }
}
