package com.alextos.thousand.presentation.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.usecase.statistics.PlayerWithTurnStatistics
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TurnsStatisticsScreen(
    goBack: () -> Unit,
) {
    val viewModel: TurnsStatisticsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(TurnsStatisticsAction.LoadStatistics)
    }

    Screen(
        modifier = Modifier,
        title = "Статистика ходов",
        goBack = goBack,
    ) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            TurnsStatisticsContent(
                modifier = modifier,
                state = state,
            )
        }
    }
}

@Composable
private fun TurnsStatisticsContent(
    modifier: Modifier,
    state: TurnsStatisticsState,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            CommonTurnsStatisticsCard(state)
        }

        item {
            PlayersTurnsStatisticsTableHeader()
        }

        if (state.players.isEmpty()) {
            item {
                EmptyTurnsStatistics()
            }
        } else {
            items(
                items = state.players,
                key = { player -> player.userId },
            ) { player ->
                PlayerTurnsStatisticsRow(player)
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CommonTurnsStatisticsCard(
    state: TurnsStatisticsState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Все игры",
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CommonTurnsStatisticsItem(
                    title = "Ходы",
                    value = state.totalTurns.toString(),
                )
                CommonTurnsStatisticsItem(
                    title = "Средний",
                    value = state.averageTurn.toScoreText(),
                )
                CommonTurnsStatisticsItem(
                    title = "Лучший",
                    value = state.bestTurn.toString(),
                )
            }
        }
    }
}

@Composable
private fun CommonTurnsStatisticsItem(
    title: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlayersTurnsStatisticsTableHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        TurnsStatisticsTableRow(
            userName = "Игрок",
            turns = "Ходы",
            averageTurn = "Средний",
            bestTurn = "Лучший",
            isHeader = true,
        )
        HorizontalDivider()
    }
}

@Composable
private fun PlayerTurnsStatisticsRow(
    player: PlayerWithTurnStatistics,
) {
    TurnsStatisticsTableRow(
        userName = player.userName,
        turns = player.turns.toString(),
        averageTurn = player.averageTurn.toScoreText(),
        bestTurn = player.bestTurn.toString(),
        isHeader = false,
    )
}

@Composable
private fun TurnsStatisticsTableRow(
    userName: String,
    turns: String,
    averageTurn: String,
    bestTurn: String,
    isHeader: Boolean,
) {
    val textStyle = if (isHeader) {
        MaterialTheme.typography.labelLarge
    } else {
        MaterialTheme.typography.bodyMedium
    }
    val fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1.4f),
            text = userName,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.weight(0.7f),
            text = turns,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.weight(0.9f),
            text = averageTurn,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.weight(0.8f),
            text = bestTurn,
            style = textStyle,
            fontWeight = fontWeight,
        )
    }
}

@Composable
private fun EmptyTurnsStatistics() {
    Text(
        modifier = Modifier.padding(vertical = 24.dp),
        text = "Пока нет данных по ходам",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun Double.toScoreText(): String {
    val rounded = (this * 10).roundToInt() / 10.0
    val text = rounded.toString()
    return if (text.endsWith(".0")) {
        text.dropLast(2)
    } else {
        text
    }
}
