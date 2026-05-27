package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.Game
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class GameListFlowBridge {
    private val channel = Channel<List<Game>>(Channel.BUFFERED)

    val flow: Flow<List<Game>> = channel.receiveAsFlow()

    fun emit(games: List<Game>) {
        channel.trySend(games)
    }

    fun closeWithError(message: String?) {
        channel.close(IllegalStateException(message ?: "Failed to load games."))
    }
}
