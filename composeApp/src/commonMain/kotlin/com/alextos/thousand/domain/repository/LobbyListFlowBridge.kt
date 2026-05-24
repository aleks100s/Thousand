package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.Lobby
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class LobbyListFlowBridge {
    private val channel = Channel<List<Lobby>>(Channel.BUFFERED)

    val flow: Flow<List<Lobby>> = channel.receiveAsFlow()

    fun emit(lobbies: List<Lobby>) {
        channel.trySend(lobbies)
    }

    fun closeWithError(message: String?) {
        channel.close(IllegalStateException(message ?: "Failed to load lobbies."))
    }
}
