package com.alextos.thousand.presentation.menu.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alextos.thousand.common.Screen

@Composable
fun StatisticsScreen(
    goBack: (() -> Unit)? = null,
    openGamesStatistics: () -> Unit,
    openTurnsStatistics: () -> Unit,
    openRollsStatistics: () -> Unit,
    openDiceStatistics: () -> Unit,
    openEventsStatistics: () -> Unit,
) {
    Screen(
        modifier = Modifier,
        title = "Статистика",
        goBack = goBack,
    ) { modifier ->
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatisticsItem(
                title = "Статистика игр",
                onClick = openGamesStatistics,
            )
            StatisticsItem(
                title = "Статистика ходов",
                onClick = openTurnsStatistics,
            )
            StatisticsItem(
                title = "Статистика бросков",
                onClick = openRollsStatistics,
            )
            StatisticsItem(
                title = "Статистика кубиков",
                onClick = openDiceStatistics,
            )
            StatisticsItem(
                title = "Статистика событий",
                onClick = openEventsStatistics,
            )
        }
    }
}

@Composable
private fun StatisticsItem(
    title: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
