package com.alextos.thousand.presentation.game.create_game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.usecase.GetAllUsersUseCase
import com.alextos.thousand.domain.usecase.SaveUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateGameViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val saveUserUseCase: SaveUserUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(CreateGameState())
    val state: StateFlow<CreateGameState> = _state.asStateFlow()
    private var initialized = false

    fun onAction(action: CreateGameAction) {
        when (action) {
            CreateGameAction.Initialize -> initialize()
            CreateGameAction.HideAddUserSheet -> hideAddUserSheet()
            CreateGameAction.SaveNewUser -> saveNewUser()
            CreateGameAction.ShowAddUserSheet -> showAddUserSheet()
            is CreateGameAction.UpdateNewUserName -> updateNewUserName(action.value)
            is CreateGameAction.ToggleUserSelection -> toggleUserSelection(action.user)
            CreateGameAction.CreateGame -> createGame()
        }
    }

    private fun initialize() {
        if (initialized) return
        initialized = true

        viewModelScope.launch {
            getAllUsersUseCase().collect { users ->
                val usersById = users.associateBy { user -> user.id }
                _state.update {
                    it.copy(
                        users = users,
                        selectedUsers = it.selectedUsers.mapNotNull { selectedUser ->
                            usersById[selectedUser.id]
                        },
                    )
                }
            }
        }
    }

    private fun showAddUserSheet() {
        _state.update {
            it.copy(
                isAddUserSheetVisible = true,
                newUserName = "",
            )
        }
    }

    private fun hideAddUserSheet() {
        _state.update {
            it.copy(
                isAddUserSheetVisible = false,
                newUserName = ""
            )
        }
    }

    private fun updateNewUserName(value: String) {
        _state.update {
            it.copy(newUserName = value)
        }
    }

    private fun toggleUserSelection(user: User) {
        _state.update { state ->
            val selectedUsers = state.selectedUsers.toMutableList()
            val existingIndex = selectedUsers.indexOfFirst { selectedUser ->
                selectedUser.id == user.id
            }
            if (existingIndex >= 0) {
                selectedUsers.removeAt(existingIndex)
            } else {
                selectedUsers.add(user)
            }
            state.copy(selectedUsers = selectedUsers)
        }
    }

    private fun saveNewUser() {
        val userName = state.value.newUserName.trim()
        if (userName.isBlank()) return

        viewModelScope.launch {
            saveUserUseCase(userName)

            _state.update {
                it.copy(
                    newUserName = "",
                    isAddUserSheetVisible = false
                )
            }
        }
    }

    private fun createGame() {

    }
}
