package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.Lobby
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GameSettingsFlowBridge {
    private val state = MutableStateFlow(
        Lobby(
            settings = GameSettings(),
            isCurrentPlayerHost = false,
        ),
    )

    val flow: Flow<Lobby> = state

    fun emit(lobby: Lobby) {
        state.value = lobby
    }
}
