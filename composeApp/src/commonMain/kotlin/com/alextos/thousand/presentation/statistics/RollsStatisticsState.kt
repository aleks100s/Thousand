package com.alextos.thousand.presentation.statistics

import com.alextos.thousand.domain.usecase.statistics.PlayerWithRollStatistics

data class RollsStatisticsState(
    val isLoading: Boolean = true,
    val totalRolls: Int = 0,
    val averageRoll: Double = 0.0,
    val bestRoll: Int = 0,
    val averageRollChain: Double = 0.0,
    val bestRollChain: Int = 0,
    val players: List<PlayerWithRollStatistics> = emptyList(),
)
