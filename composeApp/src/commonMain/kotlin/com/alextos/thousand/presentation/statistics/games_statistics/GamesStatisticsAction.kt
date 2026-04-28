package com.alextos.thousand.presentation.statistics.games_statistics

sealed interface GamesStatisticsAction {
    data object LoadStatistics : GamesStatisticsAction
}
