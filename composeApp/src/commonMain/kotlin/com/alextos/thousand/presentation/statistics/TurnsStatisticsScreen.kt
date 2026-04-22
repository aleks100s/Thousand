package com.alextos.thousand.presentation.statistics

import androidx.compose.runtime.Composable
import com.alextos.thousand.screens.common.EmptyScreen

@Composable
fun TurnsStatisticsScreen(
    goBack: () -> Unit,
) {
    EmptyScreen(
        title = "Статистика ходов",
        goBack = goBack,
    )
}
