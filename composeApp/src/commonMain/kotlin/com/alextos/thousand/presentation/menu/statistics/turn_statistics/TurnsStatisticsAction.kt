package com.alextos.thousand.presentation.menu.statistics.turn_statistics

sealed interface TurnsStatisticsAction {
    data object LoadStatistics :
        TurnsStatisticsAction
}
