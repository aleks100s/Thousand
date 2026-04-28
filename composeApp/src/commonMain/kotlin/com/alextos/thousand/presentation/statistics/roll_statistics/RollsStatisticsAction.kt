package com.alextos.thousand.presentation.statistics.roll_statistics

sealed interface RollsStatisticsAction {
    data object LoadStatistics : RollsStatisticsAction
}
