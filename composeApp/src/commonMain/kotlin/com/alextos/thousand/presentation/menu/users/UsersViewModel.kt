package com.alextos.thousand.presentation.menu.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.usecase.user.DeleteUserUseCase
import com.alextos.thousand.domain.usecase.user.GetAllUsersUseCase
import com.alextos.thousand.domain.usecase.user.UpdateUserUseCase
import com.alextos.thousand.domain.usecase.user.SaveUserUseCase
import com.alextos.thousand.domain.usecase.statistics.GamesStatisticsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsersViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val gamesStatisticsUseCase: GamesStatisticsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(UsersState())
    val state: StateFlow<UsersState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAllUsersUseCase().collect { users ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        users = users.sortedBy { user -> user.kind != UserKind.MainUser },
                    )
                }
            }
        }
        viewModelScope.launch {
            gamesStatisticsUseCase().collect { statistics ->
                val byUser = statistics.players.groupBy { it.userId }.mapValues { (_, entries) ->
                    UserStatisticsUi(entries.sumOf { it.wins }, entries.sumOf { it.games })
                }
                _state.update { it.copy(statistics = byUser) }
            }
        }
    }

    fun onAction(action: UsersAction) {
        when (action) {
            UsersAction.StartAddUser -> _state.update {
                it.copy(isAddingUser = true, editingUser = null, editingUserName = "", saveError = null)
            }
            is UsersAction.StartRenameUser -> startRenameUser(action.user)
            is UsersAction.UpdateEditingUserName -> updateEditingUserName(action.value)
            UsersAction.HideRenameUserSheet -> hideRenameUserSheet()
            UsersAction.SaveEditingUser -> saveEditingUser()
            is UsersAction.DeleteUser -> deleteUser(action.user)
            is UsersAction.RequestDeleteUser -> if (action.user.kind != UserKind.MainUser) {
                _state.update { it.copy(pendingDeleteUser = action.user) }
            }
            UsersAction.CancelDeleteUser -> _state.update { it.copy(pendingDeleteUser = null) }
        }
    }

    private fun startRenameUser(user: User) {
        _state.update {
            it.copy(
                editingUser = user,
                editingUserName = user.name,
                isAddingUser = false,
                saveError = null,
            )
        }
    }

    private fun updateEditingUserName(value: String) {
        _state.update {
            it.copy(editingUserName = value, saveError = null)
        }
    }

    private fun hideRenameUserSheet() {
        _state.update {
            it.copy(
                editingUser = null,
                editingUserName = "",
                isAddingUser = false,
                saveError = null,
            )
        }
    }

    private fun saveEditingUser() {
        val currentState = state.value
        if (currentState.canSaveEditingUser.not()) return
        _state.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            try {
                val name = currentState.editingUserName.trim()
                val user = currentState.editingUser
                if (currentState.isAddingUser) saveUserUseCase(name)
                else if (user != null) updateUserUseCase(user.copy(name = name))
                hideRenameUserSheet()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.update { it.copy(saveError = "Не удалось сохранить игрока. Попробуйте ещё раз.") }
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun deleteUser(user: User) {
        if (user.kind == UserKind.MainUser) return
        _state.update { it.copy(pendingDeleteUser = null) }

        viewModelScope.launch {
            deleteUserUseCase(user.id)
            if (state.value.editingUser?.id == user.id) {
                hideRenameUserSheet()
            }
        }
    }
}
