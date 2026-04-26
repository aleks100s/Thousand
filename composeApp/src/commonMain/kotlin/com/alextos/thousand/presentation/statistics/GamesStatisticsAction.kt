package com.alextos.thousand.presentation.statistics

sealed interface GamesStatisticsAction {
    data object LoadStatistics : GamesStatisticsAction
}
