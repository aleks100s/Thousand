package com.alextos.thousand.presentation.statistics.games_statistics

import com.alextos.thousand.domain.usecase.statistics.PlayerWithStatistics

data class GamesStatisticsState(
    val isLoading: Boolean = true,
    val finishedGamesCount: Int = 0,
    val players: List<PlayerWithStatistics> = emptyList(),
)
