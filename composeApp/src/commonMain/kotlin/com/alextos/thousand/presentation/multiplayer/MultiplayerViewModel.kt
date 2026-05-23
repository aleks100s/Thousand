package com.alextos.thousand.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.service.NativeAccountService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MultiplayerViewModel(
    private val nativeAccountService: NativeAccountService,
) : ViewModel() {
    private val _state = MutableStateFlow(MultiplayerState())
    val state: StateFlow<MultiplayerState> = _state.asStateFlow()

    init {
        observeAuthorization()
    }

    fun onAction(action: MultiplayerAction) {
        when (action) {
            MultiplayerAction.ShowLoginSheet -> showLoginSheet()
            MultiplayerAction.HideLoginSheet -> hideLoginSheet()
            is MultiplayerAction.UpdateEmail -> updateEmail(action.value)
            is MultiplayerAction.UpdatePassword -> updatePassword(action.value)
            MultiplayerAction.LogIn -> logIn()
        }
    }

    private fun observeAuthorization() {
        viewModelScope.launch {
            nativeAccountService.isAuthorized.collect { isAuthorized ->
                _state.update {
                    it.copy(
                        isAuthorized = isAuthorized,
                        isLoginSheetVisible = if (isAuthorized) false else it.isLoginSheetVisible,
                        isLoginInProgress = if (isAuthorized) false else it.isLoginInProgress,
                    )
                }
            }
        }
    }

    private fun showLoginSheet() {
        _state.update {
            it.copy(isLoginSheetVisible = true)
        }
    }

    private fun hideLoginSheet() {
        _state.update {
            it.copy(
                isLoginSheetVisible = false,
                isLoginInProgress = false,
            )
        }
    }

    private fun updateEmail(value: String) {
        _state.update {
            it.copy(email = value).withValidation()
        }
    }

    private fun updatePassword(value: String) {
        _state.update {
            it.copy(password = value).withValidation()
        }
    }

    private fun logIn() {
        val state = state.value
        if (state.canLogIn.not() || state.isLoginInProgress) return

        viewModelScope.launch {
            _state.update {
                it.copy(isLoginInProgress = true)
            }

            try {
                nativeAccountService.logIn(
                    email = state.email.trim(),
                    password = state.password,
                )
            } finally {
                _state.update {
                    it.copy(isLoginInProgress = false)
                }
            }
        }
    }

    private fun MultiplayerState.withValidation(): MultiplayerState =
        copy(canLogIn = email.trim().isNotBlank() && password.isNotBlank())
}
