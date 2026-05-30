package com.alextos.thousand.presentation.game.statistics.games_statistics

sealed interface GamesStatisticsAction {
    data object LoadStatistics :
        GamesStatisticsAction
}
