package com.alextos.thousand.presentation.multiplayer.create_lobby

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CreateLobbyViewModel : ViewModel() {
    private val _state = MutableStateFlow(CreateLobbyState())
    val state: StateFlow<CreateLobbyState> = _state.asStateFlow()

    fun onAction(action: CreateLobbyAction) {
        when (action) {
            is CreateLobbyAction.UpdateGameSettings -> updateGameSettings(action)
            CreateLobbyAction.OpenLobby -> Unit
        }
    }

    private fun updateGameSettings(action: CreateLobbyAction.UpdateGameSettings) {
        _state.update {
            it.copy(gameSettings = action.settings)
        }
    }
}
