package com.alextos.thousand.presentation.game.create_game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.usecase.CreateGameUseCase
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
    private val createGameUseCase: CreateGameUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(CreateGameState())
    val state: StateFlow<CreateGameState> = _state.asStateFlow()
    private var initialized = false

    fun onAction(action: CreateGameAction) {
        when (action) {
            CreateGameAction.Initialize -> initialize()
            CreateGameAction.HideAddUserSheet -> hideAddUserSheet()
            CreateGameAction.ConsumeCreatedGame -> consumeCreatedGame()
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
                _state.update {
                    it.copy(users = users)
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
            val set = state.selectedUsers.toMutableSet()
            if (set.contains(user)) {
                set.remove(user)
            } else {
                set.add(user)
            }
            state.copy(selectedUsers = set)
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
        viewModelScope.launch {
            val game = createGameUseCase(state.value.selectedUsers)
            _state.update {
                it.copy(createdGameId = game.id)
            }
        }
    }

    private fun consumeCreatedGame() {
        _state.update {
            it.copy(createdGameId = null)
        }
    }
}
