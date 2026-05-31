package com.alextos.thousand.presentation.menu.statistics.games_statistics

sealed interface GamesStatisticsAction {
    data object LoadStatistics :
        GamesStatisticsAction
}
