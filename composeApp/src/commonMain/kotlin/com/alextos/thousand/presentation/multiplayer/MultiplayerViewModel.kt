package com.alextos.thousand.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.service.NativeAuthenticatorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MultiplayerViewModel(
    private val nativeAuthenticatorService: NativeAuthenticatorService,
) : ViewModel() {
    private val _state = MutableStateFlow(MultiplayerState())
    val state: StateFlow<MultiplayerState> = _state.asStateFlow()

    init {
        observeAuthorization()
    }

    private fun observeAuthorization() {
        viewModelScope.launch {
            nativeAuthenticatorService.isAuthorized.collect { isAuthorized ->
                _state.update {
                    it.copy(isAuthorized = isAuthorized)
                }
            }
        }
    }
}
