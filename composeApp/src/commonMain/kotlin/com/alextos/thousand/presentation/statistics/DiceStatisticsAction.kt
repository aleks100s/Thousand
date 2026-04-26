package com.alextos.thousand.presentation.statistics

sealed interface DiceStatisticsAction {
    data object LoadStatistics : DiceStatisticsAction
}
