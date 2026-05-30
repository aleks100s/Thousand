package com.alextos.thousand.presentation.game.statistics.roll_statistics

sealed interface RollsStatisticsAction {
    data object LoadStatistics :
        RollsStatisticsAction
}
