package com.alextos.thousand.presentation.other.statistics.roll_statistics

sealed interface RollsStatisticsAction {
    data object LoadStatistics : RollsStatisticsAction
}
