package com.alextos.thousand.presentation.multiplayer.lobby

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.repository.MultiplayerManager
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.presentation.multiplayer.MultiplayerRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LobbyViewModel(
    savedStateHandle: SavedStateHandle,
    private val manager: MultiplayerManager,
    private val accountService: NativeAccountService
) : ViewModel() {
    private val route = savedStateHandle.toRoute<MultiplayerRoute.Lobby>()

    private val _state = MutableStateFlow(LobbyState(lobbyId = route.lobbyId))
    val state: StateFlow<LobbyState> = _state.asStateFlow()

    init {
        connectToLobby()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onAction(action: LobbyAction) = Unit

    private fun connectToLobby() {
        viewModelScope.launch {
            manager.connectToLobby(state.value.lobbyId)
                .catch { error ->
                    _state.update {
                        it.copy(error = error.message)
                    }
                }
                .collect { lobby ->
                    _state.update {
                        it.copy(
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
