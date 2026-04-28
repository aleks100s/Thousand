package com.alextos.thousand.presentation.statistics.turn_statistics

import com.alextos.thousand.domain.usecase.statistics.PlayerWithTurnStatistics

data class TurnsStatisticsState(
    val isLoading: Boolean = true,
    val totalTurns: Int = 0,
    val averageTurn: Double = 0.0,
    val bestTurn: Int = 0,
    val players: List<PlayerWithTurnStatistics> = emptyList(),
)
