package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.RemoteGame
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class GameListFlowBridge {
    private val channel = Channel<List<RemoteGame>>(Channel.BUFFERED)

    val flow: Flow<List<RemoteGame>> = channel.receiveAsFlow()

    fun emit(games: List<RemoteGame>) {
        channel.trySend(games)
    }

    fun closeWithError(message: String?) {
        channel.close(IllegalStateException(message ?: "Failed to load games."))
    }
}
