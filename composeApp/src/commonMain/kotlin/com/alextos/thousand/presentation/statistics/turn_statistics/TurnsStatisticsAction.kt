package com.alextos.thousand.presentation.statistics.turn_statistics

sealed interface TurnsStatisticsAction {
    data object LoadStatistics : TurnsStatisticsAction
}
