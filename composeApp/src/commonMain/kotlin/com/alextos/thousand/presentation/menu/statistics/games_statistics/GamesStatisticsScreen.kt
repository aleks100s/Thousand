package com.alextos.thousand.presentation.menu.statistics.games_statistics

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
fun GamesStatisticsScreen(goBack: () -> Unit) {
    val viewModel: GamesStatisticsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.onAction(GamesStatisticsAction.LoadStatistics) }
    Screen(modifier = Modifier, constrainListWidth = true, title = "Статистика игр", goBack = goBack) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), Alignment.Center) { LoadingIndicator() }
        } else {
            GamesStatisticsContent(modifier, state)
        }
    }
}

@Composable
internal fun GamesStatisticsContent(modifier: Modifier, state: GamesStatisticsState) {
    LazyColumn(
        modifier.fillMaxSize().navigationBarsPadding(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "summary", contentType = "summary") {
            Column {
                StatisticsSummary(metrics = listOf(StatisticsMetricUi("Завершённые игры", state.finishedGamesCount.toString(), Res.drawable.statistics_bar_chart)))
                Spacer(Modifier.height(8.dp))
            }
        }
        item(key = "players-header", contentType = "players-header") {
            StatisticsTableRow("Игрок", listOf("Игры", "Победы", "Поражения"), header = true)
        }
        if (state.players.isEmpty()) {
            item(key = "empty", contentType = "empty") { EmptyStatisticsCard("Пока нет данных по игрокам") }
        } else {
            items(state.players, key = { it.userId }, contentType = { "player-statistics" }) { player ->
                StatisticsTableRow(user = player.user, name = player.userName, values = listOf(player.games.toString(), player.wins.toString(), player.losses.toString()))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun GamesStatisticsPreview() {
    com.alextos.thousand.application.theme.ThousandTheme {
        com.alextos.thousand.application.theme.AppBackground {
            Screen(Modifier, title = "Статистика игр", goBack = {}) { modifier ->
                GamesStatisticsContent(modifier, GamesStatisticsState(isLoading = false, finishedGamesCount = 45, players = listOf(com.alextos.thousand.domain.usecase.statistics.PlayerWithStatistics("1", "Легендарный Хомяк", 45, 28, 17))))
            }
        }
    }
}
