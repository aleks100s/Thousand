package com.alextos.thousand.presentation.other.statistics

import kotlinx.serialization.Serializable

sealed interface StatisticsRoute {
    @Serializable
    data object Statistics : StatisticsRoute

    @Serializable
    data object GamesStatistics : StatisticsRoute

    @Serializable
    data object TurnsStatistics : StatisticsRoute

    @Serializable
    data object RollsStatistics : StatisticsRoute

    @Serializable
    data object DiceStatistics : StatisticsRoute

    @Serializable
    data object EventsStatistics : StatisticsRoute
}
