package com.alextos.thousand.presentation.game

import kotlinx.serialization.Serializable

sealed interface GameRoute {
    @Serializable
    data object GamesList: GameRoute
    @Serializable
    data object CreateGame: GameRoute
    @Serializable
    data object Game: GameRoute
    @Serializable
    data object GameScore: GameRoute
}