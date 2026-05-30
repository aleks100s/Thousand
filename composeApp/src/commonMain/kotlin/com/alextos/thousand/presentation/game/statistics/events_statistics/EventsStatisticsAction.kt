package com.alextos.thousand.presentation.game.statistics.events_statistics

sealed interface EventsStatisticsAction {
    data object LoadStatistics :
        EventsStatisticsAction
}
