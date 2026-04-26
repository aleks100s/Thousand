package com.alextos.thousand.presentation.statistics

sealed interface TurnsStatisticsAction {
    data object LoadStatistics : TurnsStatisticsAction
}
