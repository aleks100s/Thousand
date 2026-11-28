package com.alextos.thousand.presentation.menu.multiplayer.player_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.usecase.DeletePlayerProfileUseCase
import com.alextos.thousand.domain.usecase.user.GetAllUsersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerProfileViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val deletePlayerProfileUseCase: DeletePlayerProfileUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(PlayerProfileState())
    val state: StateFlow<PlayerProfileState> = _state.asStateFlow()

    init {
        observeMainUser()
    }

    fun onAction(action: PlayerProfileAction) {
        when (action) {
            PlayerProfileAction.ShowDeleteAccountDialog -> showDeleteAccountDialog()
            PlayerProfileAction.HideDeleteAccountDialog -> hideDeleteAccountDialog()
            PlayerProfileAction.DeleteAccount -> deleteAccount()
        }
    }

    private fun observeMainUser() {
        viewModelScope.launch {
            getAllUsersUseCase().collect { users ->
                _state.update { current ->
                    current.copy(user = users.firstOrNull { it.kind == UserKind.MainUser })
                }
            }
        }
    }

    private fun showDeleteAccountDialog() {
        _state.update {
            it.copy(isDeleteAccountDialogVisible = true, deleteError = null)
        }
    }

    private fun hideDeleteAccountDialog() {
        _state.update {
            it.copy(isDeleteAccountDialogVisible = false)
        }
    }

    private fun deleteAccount() {
        if (state.value.isDeleteInProgress) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isDeleteInProgress = true,
                    deleteError = null,
                )
            }

            try {
                deletePlayerProfileUseCase()
                _state.update {
                    it.copy(isDeleteAccountDialogVisible = false)
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(deleteError = error.message ?: "Не удалось удалить профиль. Попробуйте ещё раз.")
                }
            } finally {
                _state.update {
                    it.copy(isDeleteInProgress = false)
                }
            }
        }
    }
}
