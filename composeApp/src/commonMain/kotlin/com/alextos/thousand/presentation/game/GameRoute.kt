package com.alextos.thousand.presentation.game

import kotlinx.serialization.Serializable

sealed interface GameRoute {
    @Serializable
    data object GamesList: GameRoute
    @Serializable
    data object CreateGame: GameRoute
    @Serializable
    data class PlayGame(val gameId: Long): GameRoute
    @Serializable
    data class GameScore(val gameId: Long): GameRoute
}
