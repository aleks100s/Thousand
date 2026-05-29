package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.RemoteGame
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class GameFlowBridge {
    private val channel = Channel<RemoteGame>(Channel.BUFFERED)

    val flow: Flow<RemoteGame> = channel.receiveAsFlow()

    fun emit(game: RemoteGame) {
        channel.trySend(game)
    }

    fun closeWithError(message: String?) {
        channel.close(IllegalStateException(message ?: "Failed to load game."))
    }
}
