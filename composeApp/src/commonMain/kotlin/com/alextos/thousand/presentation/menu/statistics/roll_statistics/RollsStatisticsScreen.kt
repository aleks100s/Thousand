package com.alextos.thousand.presentation.menu.statistics.roll_statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
fun RollsStatisticsScreen(goBack: () -> Unit) {
    val viewModel: RollsStatisticsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.onAction(RollsStatisticsAction.LoadStatistics) }
    Screen(modifier = Modifier, constrainListWidth = true, title = "Статистика бросков", goBack = goBack) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), Alignment.Center) { LoadingIndicator() }
        } else {
            RollsStatisticsContent(modifier, state)
        }
    }
}

@Composable
internal fun RollsStatisticsContent(modifier: Modifier, state: RollsStatisticsState) {
    val tableScroll = rememberScrollState()
    LazyColumn(
        modifier.fillMaxSize().navigationBarsPadding(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "summary", contentType = "summary") {
            Column {
                StatisticsSummary(metrics = listOf(
                    StatisticsMetricUi("Броски", state.totalRolls.toString(), Res.drawable.rules_casino),
                    StatisticsMetricUi("Средний", state.averageRoll.toStatisticsText(), Res.drawable.statistics_functions, StatisticsPurple),
                    StatisticsMetricUi("Лучший", state.bestRoll.toString(), Res.drawable.statistics_emoji_events, StatisticsGold),
                    StatisticsMetricUi("Средняя цепь", state.averageRollChain.toStatisticsText(), Res.drawable.statistics_link),
                    StatisticsMetricUi("Лучшая цепь", state.bestRollChain.toString(), Res.drawable.rules_trending_up, StatisticsGold),
                ))
                Spacer(Modifier.height(8.dp))
            }
        }
        item(key = "players-header", contentType = "players-header") {
            StatisticsTableRow("Игрок", listOf("Броски", "Средний", "Лучший", "Средняя цепь", "Лучшая цепь"), header = true, scrollState = tableScroll)
        }
        if (state.players.isEmpty()) {
            item(key = "empty", contentType = "empty") { EmptyStatisticsCard("Пока нет данных по броскам") }
        } else {
            items(state.players, key = { it.userId }, contentType = { "player-statistics" }) { player ->
                StatisticsTableRow(user = player.user, name = player.userName, values = listOf(player.rolls.toString(), player.averageRoll.toStatisticsText(), player.bestRoll.toString(),
                    player.averageRollChain.toStatisticsText(), player.bestRollChain.toString()), scrollState = tableScroll)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun RollsStatisticsPreview() {
    com.alextos.thousand.application.theme.ThousandTheme {
        com.alextos.thousand.application.theme.AppBackground {
            Screen(Modifier, title = "Статистика бросков", goBack = {}) { modifier ->
                RollsStatisticsContent(modifier, RollsStatisticsState(isLoading = false, totalRolls = 165, averageRoll = 16.1, bestRoll = 205, averageRollChain = 2.2, bestRollChain = 7, players = listOf(com.alextos.thousand.domain.usecase.statistics.PlayerWithRollStatistics("1", "Легендарный Хомяк", 78, 19.1, 205, 2.0, 7))))
            }
        }
    }
}
