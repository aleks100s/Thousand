package com.alextos.thousand.presentation.menu.statistics.events_statistics

sealed interface EventsStatisticsAction {
    data object LoadStatistics :
        EventsStatisticsAction
}
