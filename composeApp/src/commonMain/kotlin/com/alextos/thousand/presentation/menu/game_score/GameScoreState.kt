package com.alextos.thousand.presentation.menu.game_score

import com.alextos.thousand.domain.models.Game

data class GameScoreState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val turns: List<TurnHistoryUi> = emptyList(),
    val scoreSeries: List<PlayerScoreSeries> = emptyList(),
    val playerStatistics: List<GameResultsPlayerStatistics> = emptyList(),
) {
    val turnRows: List<TurnHistoryRow> = turns.toHistoryRows()

    val showSummary: Boolean get() = game?.isFinished() == true

    val title: String
        get() = game?.let { "Результаты игры №${it.id}" } ?: ""
}
