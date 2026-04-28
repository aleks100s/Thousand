package com.alextos.thousand.presentation.statistics.dice_statistics

sealed interface DiceStatisticsAction {
    data object LoadStatistics : DiceStatisticsAction
}
