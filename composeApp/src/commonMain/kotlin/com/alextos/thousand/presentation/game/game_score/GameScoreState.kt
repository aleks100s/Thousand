package com.alextos.thousand.presentation.game.game_score

import com.alextos.thousand.domain.models.Game

data class GameScoreState(
    val isLoading: Boolean = true,
    val game: Game? = null,
) {
    val title: String
        get() = game?.let { "Игра №${it.id}" } ?: ""
}
