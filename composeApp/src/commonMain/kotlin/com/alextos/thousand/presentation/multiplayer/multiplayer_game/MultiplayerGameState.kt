package com.alextos.thousand.presentation.multiplayer.multiplayer_game

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.usecase.game.server.GameState

data class MultiplayerGameState(
    val gameCode: String = "",
    val isHost: Boolean = false,
    val error: String? = null,
    val game: RemoteGame? = null,
) {
    val gameState: GameState
        get() = GameState(
            isLoading = game == null,
            game = game?.let { remoteGame ->
                Game(
                    id = remoteGame.id,
                    settings = remoteGame.settings,
                    players = remoteGame.players,
                )
            },
            currentPlayer = game?.currentPlayer,
            currentTurn = game?.currentTurn.orEmpty(),
            currentRoll = game?.currentRoll,
        )
}
