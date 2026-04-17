package com.alextos.thousand.presentation.game.play_game

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Turn

data class PlayGameState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val turns: List<Turn> = emptyList()
) {
    val title: String
        get() = game?.let { "Игра №${it.id}" } ?: ""
}
