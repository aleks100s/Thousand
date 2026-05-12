package com.alextos.thousand.presentation.game.game_results

import com.alextos.thousand.domain.models.Game

data class GameResultsState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val scoreSeries: List<PlayerScoreSeries> = emptyList(),
    val playerStatistics: List<GameResultsPlayerStatistics> = emptyList(),
) {
    val title: String
        get() = game?.let { "Итоги игры №${it.id}" } ?: "Итоги игры"
}

data class PlayerScoreSeries(
    val userId: Long,
    val playerName: String,
    val points: List<Int>,
)

data class GameResultsPlayerStatistics(
    val userId: Long,
    val playerName: String,
    val gameAverageTurn: Double,
    val globalAverageTurn: Double,
    val gameAverageRoll: Double,
    val globalAverageRoll: Double,
)
