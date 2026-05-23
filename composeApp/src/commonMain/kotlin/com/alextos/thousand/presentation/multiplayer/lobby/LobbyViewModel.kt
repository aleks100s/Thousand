package com.alextos.thousand.presentation.multiplayer.lobby

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.alextos.thousand.presentation.multiplayer.MultiplayerRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LobbyViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<MultiplayerRoute.Lobby>()

    private val _state = MutableStateFlow(
        LobbyState(
            lobbyId = route.lobbyId,
        ),
    )
    val state: StateFlow<LobbyState> = _state.asStateFlow()

    @Suppress("UNUSED_PARAMETER")
    fun onAction(action: LobbyAction) = Unit
}
