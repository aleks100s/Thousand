package com.alextos.thousand.presentation.menu.statistics.events_statistics

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
fun EventsStatisticsScreen(goBack: () -> Unit) {
    val viewModel: EventsStatisticsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.onAction(EventsStatisticsAction.LoadStatistics) }
    Screen(modifier = Modifier, constrainListWidth = true, title = "Статистика событий", goBack = goBack) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), Alignment.Center) { LoadingIndicator() }
        } else {
            EventsStatisticsContent(modifier, state)
        }
    }
}

@Composable
internal fun EventsStatisticsContent(modifier: Modifier, state: EventsStatisticsState) {
    LazyColumn(
        modifier.fillMaxSize().navigationBarsPadding(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "summary", contentType = "summary") {
            Column {
                StatisticsSummary(metrics = listOf(
                    StatisticsMetricUi("Упал с 555", state.pitFalls.toString(), Res.drawable.rules_trip_origin),
                    StatisticsMetricUi("Обогнал другого", state.overtakes.toString(), Res.drawable.rules_trending_up, StatisticsPurple),
                    StatisticsMetricUi("Три болта", state.tripleBolts.toString(), Res.drawable.rules_gavel, StatisticsGold),
                ), title = "Все события")
                Spacer(Modifier.height(8.dp))
            }
        }
        item(key = "players-header", contentType = "players-header") {
            StatisticsTableRow("Игрок", listOf("555", "Обгоны", "Болты"), header = true)
        }
        if (state.players.isEmpty()) {
            item(key = "empty", contentType = "empty") { EmptyStatisticsCard("Пока нет событий для статистики") }
        } else {
            items(state.players, key = { it.userId }, contentType = { "player-statistics" }) { player ->
                StatisticsTableRow(user = player.user, name = player.userName, values = listOf(player.pitFalls.toString(), player.overtakes.toString(), player.tripleBolts.toString()))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun EventsStatisticsPreview() {
    com.alextos.thousand.application.theme.ThousandTheme {
        com.alextos.thousand.application.theme.AppBackground {
            Screen(Modifier, title = "Статистика событий", goBack = {}) { modifier ->
                EventsStatisticsContent(modifier, EventsStatisticsState(isLoading = false, pitFalls = 2, overtakes = 8, tripleBolts = 4, players = listOf(com.alextos.thousand.domain.usecase.statistics.PlayerWithEventsStatistics("1", "Легендарный Хомяк", 1, 3, 2))))
            }
        }
    }
}
