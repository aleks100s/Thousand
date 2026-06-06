package com.alextos.thousand.presentation.multiplayer.player_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.usecase.DeletePlayerProfileUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerProfileViewModel(
    private val accountService: NativeAccountService,
    private val deletePlayerProfileUseCase: DeletePlayerProfileUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(PlayerProfileState())
    val state: StateFlow<PlayerProfileState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PlayerProfileEvent>()
    val events: SharedFlow<PlayerProfileEvent> = _events.asSharedFlow()

    init {
        observeUserProfile()
    }

    fun onAction(action: PlayerProfileAction) {
        when (action) {
            PlayerProfileAction.ShowLogoutDialog -> showLogoutDialog()
            PlayerProfileAction.HideLogoutDialog -> hideLogoutDialog()
            PlayerProfileAction.SignOut -> signOut()
            PlayerProfileAction.DeleteAccount -> deleteAccount()
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            accountService.userProfile.collect { user ->
                _state.update {
                    it.copy(
                        userId = user?.id.orEmpty(),
                        username = user?.name.orEmpty(),
                    )
                }
            }
        }
    }

    private fun showLogoutDialog() {
        _state.update {
            it.copy(isLogoutDialogVisible = true)
        }
    }

    private fun hideLogoutDialog() {
        _state.update {
            it.copy(isLogoutDialogVisible = false)
        }
    }

    private fun signOut() {
        accountService.signOut()
        _state.update {
            it.copy(isLogoutDialogVisible = false)
        }
        viewModelScope.launch {
            _events.emit(PlayerProfileEvent.GoBack)
        }
    }

    private fun deleteAccount() {
        val userId = state.value.userId
        if (userId.isBlank() || state.value.isDeleteInProgress) return

        viewModelScope.launch {
            _state.update {
                it.copy(isDeleteInProgress = true)
            }

            try {
                deletePlayerProfileUseCase(userId)
                _events.emit(PlayerProfileEvent.GoBack)
            } catch (_: Exception) {
            } finally {
                _state.update {
                    it.copy(isDeleteInProgress = false)
                }
            }
        }
    }
}
