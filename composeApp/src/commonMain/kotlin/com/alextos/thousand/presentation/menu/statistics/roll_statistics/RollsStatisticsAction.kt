package com.alextos.thousand.presentation.menu.statistics.roll_statistics

sealed interface RollsStatisticsAction {
    data object LoadStatistics :
        RollsStatisticsAction
}
