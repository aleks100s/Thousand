package com.alextos.thousand.presentation.multiplayer.multiplayer_game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.alextos.thousand.presentation.multiplayer.MultiplayerRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MultiplayerGameViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<MultiplayerRoute.MultiplayerGame>()

    private val _state = MutableStateFlow(
        MultiplayerGameState(
            lobbyId = route.lobbyId,
        ),
    )
    val state: StateFlow<MultiplayerGameState> = _state.asStateFlow()

    @Suppress("UNUSED_PARAMETER")
    fun onAction(action: MultiplayerGameAction) = Unit
}
