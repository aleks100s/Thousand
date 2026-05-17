package com.alextos.thousand.presentation.other.statistics.events_statistics

sealed interface EventsStatisticsAction {
    data object LoadStatistics : EventsStatisticsAction
}
