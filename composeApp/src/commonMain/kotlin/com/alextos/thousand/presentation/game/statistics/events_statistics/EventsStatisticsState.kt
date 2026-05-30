package com.alextos.thousand.presentation.game.statistics.events_statistics

import com.alextos.thousand.domain.usecase.statistics.PlayerWithEventsStatistics

data class EventsStatisticsState(
    val isLoading: Boolean = true,
    val pitFalls: Int = 0,
    val overtakes: Int = 0,
    val tripleBolts: Int = 0,
    val players: List<PlayerWithEventsStatistics> = emptyList(),
)
