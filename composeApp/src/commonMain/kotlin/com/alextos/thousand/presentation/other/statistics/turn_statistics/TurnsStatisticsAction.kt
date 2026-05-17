package com.alextos.thousand.presentation.other.statistics.turn_statistics

sealed interface TurnsStatisticsAction {
    data object LoadStatistics :
        TurnsStatisticsAction
}
