package com.alextos.thousand.domain.usecase.game.server

import com.alextos.thousand.domain.models.Game
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface GameServer {
    val state: StateFlow<GameState>
    val events: SharedFlow<GameEvent>

    suspend fun initGame(game: Game?)

    suspend fun sendAction(action: GameAction)
}
