package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.Lobby
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class GameSettingsFlowBridge {
    private val channel = Channel<Lobby>(Channel.BUFFERED)

    val flow: Flow<Lobby> = channel.receiveAsFlow()

    fun emit(lobby: Lobby) {
        channel.trySend(lobby)
    }

    fun closeWithError(message: String?) {
        channel.close(IllegalStateException(message ?: "Failed to connect to lobby."))
    }
}
