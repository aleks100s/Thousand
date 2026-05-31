package com.alextos.thousand.presentation.menu.statistics.dice_statistics

sealed interface DiceStatisticsAction {
    data object LoadStatistics :
        DiceStatisticsAction
}
