package com.alextos.thousand.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.service.NativeAuthenticatorService
import com.alextos.thousand.domain.usecase.user.GetAllUsersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val nativeAuthenticatorService: NativeAuthenticatorService,
) : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        observeUsers()
    }

    private fun observeUsers() {
        viewModelScope.launch {
            combine(
                getAllUsersUseCase(),
                nativeAuthenticatorService.hideMultiplayer,
            ) { users, hideMultiplayer ->
                AppState(
                    isLoading = false,
                    isFirstUserRequired = users.none { user -> user.kind == UserKind.MainUser },
                    hideMultiplayer = hideMultiplayer,
                )
            }.collect { state ->
                _state.update {
                    state
                }
            }
        }
    }
}
