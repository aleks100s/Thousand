package com.alextos.thousand.presentation.game.game_score

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Turn

data class GameScoreState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val turns: List<Turn> = emptyList()
) {
    val title: String
        get() = game?.let { "Результаты игры №${it.id}" } ?: ""
}
