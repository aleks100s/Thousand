package com.alextos.thousand.presentation.other.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.usecase.user.DeleteUserUseCase
import com.alextos.thousand.domain.usecase.user.GetAllUsersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsersViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
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
            is UsersAction.DeleteUser -> deleteUser(action.user)
        }
    }

    private fun deleteUser(user: User) {
        viewModelScope.launch {
            deleteUserUseCase(user.id)
        }
    }
}
