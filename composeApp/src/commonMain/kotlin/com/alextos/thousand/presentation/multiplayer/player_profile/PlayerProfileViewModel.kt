package com.alextos.thousand.presentation.multiplayer.player_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.service.NativeAccountService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerProfileViewModel(
    private val accountService: NativeAccountService,
) : ViewModel() {
    private val _state = MutableStateFlow(PlayerProfileState())
    val state: StateFlow<PlayerProfileState> = _state.asStateFlow()

    init {
        observeUserProfile()
    }

    fun onAction(action: PlayerProfileAction) {
        when (action) {
            PlayerProfileAction.ShowLogoutDialog -> showLogoutDialog()
            PlayerProfileAction.HideLogoutDialog -> hideLogoutDialog()
            PlayerProfileAction.SignOut -> signOut()
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            accountService.userProfile.collect { user ->
                _state.update {
                    it.copy(username = user?.name.orEmpty())
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
            it.copy(
                isLogoutDialogVisible = false,
                isSignedOut = true,
            )
        }
    }
}
