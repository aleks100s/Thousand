package com.alextos.thousand.presentation.menu.multiplayer.lobby

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.repository.LobbyNotFoundException
import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.presentation.menu.multiplayer.MultiplayerRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _events = Channel<LobbyEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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
            val startedLobby = repository.connectToLobby(lobbyId)
                .catch { error ->
                    _state.update {
                        if (error is LobbyNotFoundException) {
                            it.copy(isClosed = true)
                        } else {
                            it.copy(error = error.message)
                        }
                    }
                }
                .firstOrNull { lobby ->
                    if (lobby.game.isEmpty()) {
                        _state.update {
                            it.copy(
                                lobbyId = lobby.id,
                                gameSettings = lobby.settings,
                                players = lobby.players,
                                hostId = lobby.host,
                                error = null,
                                isHost = lobby.host == accountService.userProfile.value?.id
                            )
                        }
                    }
                    lobby.game.isNotEmpty()
                }
            startedLobby?.let { _events.send(LobbyEvent.StartGame(it.game)) }
        }
    }

    private fun leaveGame() {
        viewModelScope.launch {
            try {
                _events.send(LobbyEvent.Disconnect)
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
