package com.alextos.thousand.presentation.statistics

sealed interface RollsStatisticsAction {
    data object LoadStatistics : RollsStatisticsAction
}
