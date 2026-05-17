package com.alextos.thousand.presentation.other.statistics.games_statistics

sealed interface GamesStatisticsAction {
    data object LoadStatistics :
        com.alextos.thousand.presentation.other.statistics.games_statistics.GamesStatisticsAction
}
