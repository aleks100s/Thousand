package com.alextos.thousand.presentation.game.statistics.turn_statistics

sealed interface TurnsStatisticsAction {
    data object LoadStatistics :
        TurnsStatisticsAction
}
