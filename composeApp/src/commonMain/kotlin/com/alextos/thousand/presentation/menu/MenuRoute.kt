package com.alextos.thousand.presentation.menu

import kotlinx.serialization.Serializable

sealed interface MenuRoute {
    @Serializable
    data object Menu : MenuRoute

    @Serializable
    data object Rules : MenuRoute

    @Serializable
    data object TutorialGame : MenuRoute

    @Serializable
    data object GamesList : MenuRoute

    @Serializable
    data object CreateGame : MenuRoute

    @Serializable
    data object Statistics : MenuRoute

    @Serializable
    data object Users : MenuRoute

    @Serializable
    data object Multiplayer : MenuRoute

    @Serializable
    data class PlayGame(val gameId: Long) : MenuRoute

    @Serializable
    data class GameScore(val gameId: Long) : MenuRoute

    @Serializable
    data class GameResults(val gameId: Long) : MenuRoute
}
