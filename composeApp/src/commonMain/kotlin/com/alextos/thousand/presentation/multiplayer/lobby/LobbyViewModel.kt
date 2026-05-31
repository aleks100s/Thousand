package com.alextos.thousand.presentation.multiplayer.lobby

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.presentation.multiplayer.MultiplayerRoute
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LobbyViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MultiplayerRepository,
    private val accountService: NativeAccountService
) : ViewModel() {
    private val lobbyId = savedStateHandle.toRoute<MultiplayerRoute.Lobby>().lobbyId

    private val _state = MutableStateFlow(LobbyState())
    val state: StateFlow<LobbyState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LobbyEvent>()
    val events = _events.asSharedFlow()

    init {
        connectToLobby()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onAction(action: LobbyAction) {
        when (action) {
            LobbyAction.LeaveGame -> leaveGame()
            LobbyAction.StartGame -> startGame()
        }
    }

    private fun connectToLobby() {
        viewModelScope.launch {
            repository.connectToLobby(lobbyId)
                .catch { error ->
                    _state.update {
                        it.copy(error = error.message)
                    }
                }
                .collect { lobby ->
                    if (lobby.game.isNotEmpty()) {
                        _events.emit(LobbyEvent.StartGame(lobby.game))
                    } else {
                        _state.update {
                            it.copy(
                                lobbyId = lobby.id,
                                gameSettings = lobby.settings,
                                players = lobby.players,
                                error = null,
                                isHost = lobby.host == accountService.userProfile.value?.id
                            )
                        }
                    }
                }
        }
    }

    private fun leaveGame() {
        viewModelScope.launch {
            try {
                _events.emit(LobbyEvent.Disconnect)
                repository.disconnectFromLobby(lobbyId)
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    private fun startGame() {
        viewModelScope.launch {
            repository.startGame(lobbyId)
        }
    }
}
