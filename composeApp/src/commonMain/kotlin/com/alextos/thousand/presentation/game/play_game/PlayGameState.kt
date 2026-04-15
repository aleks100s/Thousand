package com.alextos.thousand.presentation.game.play_game

import com.alextos.thousand.domain.models.Game

data class PlayGameState(
    val isLoading: Boolean = true,
    val game: Game? = null,
) {
    val title: String
        get() = game?.let { "Игра №${it.id}" } ?: ""
}
