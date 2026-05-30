package com.alextos.thousand.presentation.game.statistics.dice_statistics

sealed interface DiceStatisticsAction {
    data object LoadStatistics :
        DiceStatisticsAction
}
