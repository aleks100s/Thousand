package com.alextos.thousand.presentation.menu.statistics.turn_statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.menu.statistics.*
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TurnsStatisticsScreen(goBack: () -> Unit) {
    val viewModel: TurnsStatisticsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.onAction(TurnsStatisticsAction.LoadStatistics) }
    Screen(modifier = Modifier, constrainListWidth = true, title = "Статистика ходов", goBack = goBack) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), Alignment.Center) { LoadingIndicator() }
        } else {
            TurnsStatisticsContent(modifier, state)
        }
    }
}

@Composable
internal fun TurnsStatisticsContent(modifier: Modifier, state: TurnsStatisticsState) {
    LazyColumn(
        modifier.fillMaxSize().navigationBarsPadding(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "summary", contentType = "summary") {
            Column {
                StatisticsSummary(metrics = listOf(
                    StatisticsMetricUi("Ходы", state.totalTurns.toString(), Res.drawable.statistics_bar_chart),
                    StatisticsMetricUi("Средний", state.averageTurn.toStatisticsText(), Res.drawable.statistics_functions, StatisticsPurple),
                    StatisticsMetricUi("Лучший", state.bestTurn.toString(), Res.drawable.statistics_emoji_events, StatisticsGold),
                ))
                Spacer(Modifier.height(8.dp))
            }
        }
        item(key = "players-header", contentType = "players-header") {
            StatisticsTableRow("Игрок", listOf("Ходы", "Средний", "Лучший"), header = true)
        }
        if (state.players.isEmpty()) {
            item(key = "empty", contentType = "empty") { EmptyStatisticsCard("Пока нет данных по ходам") }
        } else {
            items(state.players, key = { it.userId }, contentType = { "player-statistics" }) { player ->
                StatisticsTableRow(user = player.user, name = player.userName, values = listOf(player.turns.toString(), player.averageTurn.toStatisticsText(), player.bestTurn.toString()))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun TurnsStatisticsPreview() {
    com.alextos.thousand.application.theme.ThousandTheme {
        com.alextos.thousand.application.theme.AppBackground {
            Screen(Modifier, title = "Статистика ходов", goBack = {}) { modifier ->
                TurnsStatisticsContent(modifier, TurnsStatisticsState(isLoading = false, totalTurns = 76, averageTurn = 27.9, bestTurn = 220, players = listOf(com.alextos.thousand.domain.usecase.statistics.PlayerWithTurnStatistics("1", "Легендарный Хомяк", 39, 33.7, 220))))
            }
        }
    }
}
