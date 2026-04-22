package com.alextos.thousand.presentation.statistics

import androidx.compose.runtime.Composable
import com.alextos.thousand.screens.common.EmptyScreen

@Composable
fun DiceStatisticsScreen(
    goBack: () -> Unit,
) {
    EmptyScreen(
        title = "Статистика кубиков",
        goBack = goBack,
    )
}
