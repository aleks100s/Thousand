package com.alextos.thousand.presentation.other.statistics.dice_statistics

sealed interface DiceStatisticsAction {
    data object LoadStatistics : DiceStatisticsAction
}
