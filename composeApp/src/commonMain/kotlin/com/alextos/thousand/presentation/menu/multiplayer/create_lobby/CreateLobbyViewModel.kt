package com.alextos.thousand.presentation.menu.multiplayer.create_lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.repository.MultiplayerRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateLobbyViewModel(
    private val repository: MultiplayerRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CreateLobbyState())
    val state: StateFlow<CreateLobbyState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateLobbyEvent>()
    val events: SharedFlow<CreateLobbyEvent> = _events.asSharedFlow()

    fun onAction(action: CreateLobbyAction) {
        when (action) {
            is CreateLobbyAction.UpdateGameSettings -> updateGameSettings(action)
            CreateLobbyAction.OpenLobby -> openLobby()
        }
    }

    private fun updateGameSettings(action: CreateLobbyAction.UpdateGameSettings) {
        _state.update {
            it.copy(gameSettings = action.settings)
        }
    }

    private fun openLobby() {
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            val lobbyId = repository.createLobby(state.value.gameSettings)
            _events.emit(CreateLobbyEvent.OpenLobby(lobbyId = lobbyId))
        }
    }
}
