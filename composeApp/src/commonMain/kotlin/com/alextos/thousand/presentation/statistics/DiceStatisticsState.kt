package com.alextos.thousand.presentation.statistics

import com.alextos.thousand.domain.usecase.statistics.DieValueDistribution
import com.alextos.thousand.domain.usecase.statistics.PlayerWithDiceStatistics

data class DiceStatisticsState(
    val isLoading: Boolean = true,
    val totalDice: Int = 0,
    val averageDie: Double = 0.0,
    val distribution: List<DieValueDistribution> = emptyList(),
    val players: List<PlayerWithDiceStatistics> = emptyList(),
)
