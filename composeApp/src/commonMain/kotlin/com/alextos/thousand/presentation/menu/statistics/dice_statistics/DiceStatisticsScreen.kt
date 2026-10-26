package com.alextos.thousand.presentation.menu.statistics.dice_statistics

import com.alextos.thousand.domain.models.User

import com.alextos.thousand.application.theme.AppShapes
import com.alextos.thousand.presentation.components.AppBottomSheet as ModalBottomSheet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.domain.usecase.statistics.DieValueDistribution
import com.alextos.thousand.presentation.components.appearanceColor
import com.alextos.thousand.presentation.components.SingleDieView
import com.alextos.thousand.presentation.menu.statistics.*
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiceStatisticsScreen(goBack: () -> Unit) {
    val viewModel: DiceStatisticsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var histogram by remember { mutableStateOf<HistogramState?>(null) }
    LaunchedEffect(Unit) { viewModel.onAction(DiceStatisticsAction.LoadStatistics) }
    Screen(modifier = Modifier, constrainListWidth = true, title = "Статистика кубиков", goBack = goBack) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), Alignment.Center) { LoadingIndicator() }
        } else {
            DiceStatisticsContent(modifier, state) { histogram = it }
        }
    }
    histogram?.let { data ->
        ModalBottomSheet(
            onDismissRequest = { histogram = null },
        ) {
            DiceDistributionHistogram(data.title, data.distribution, data.user)
        }
    }
}

@Composable
private fun DiceStatisticsContent(modifier: Modifier, state: DiceStatisticsState, showHistogram: (HistogramState) -> Unit) {
    LazyColumn(
        modifier.fillMaxSize().navigationBarsPadding(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "summary", contentType = "summary") {
            Column {
                StatisticsSummary(
                    metrics = listOf(
                        StatisticsMetricUi("Кубики", state.totalDice.toString(), Res.drawable.rules_casino),
                        StatisticsMetricUi("Средний", state.averageDie.toStatisticsText(), Res.drawable.statistics_functions, StatisticsPurple),
                    ),
                    onHistogram = { showHistogram(HistogramState("Распределение кубиков", state.distribution)) },
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        item(key = "players-header", contentType = "players-header") {
            StatisticsTableRow("Игрок", listOf("Кубики", "Средний"), header = true, showAction = true)
        }
        if (state.players.isEmpty()) {
            item(key = "empty", contentType = "empty") { EmptyStatisticsCard("Пока нет данных по кубикам") }
        } else {
            items(state.players, key = { it.userId }, contentType = { "player-statistics" }) { player ->
                StatisticsTableRow(user = player.user, name = player.userName, values = listOf(player.dice.toString(), player.averageDie.toStatisticsText()),
                    showAction = true,
                    onClick = { showHistogram(HistogramState("Распределение: ${player.userName}", player.distribution, player.user)) })
            }
        }
    }
}

@Composable
private fun DiceDistributionHistogram(title: String, distribution: List<DieValueDistribution>, user: User?) {
    val maxCount = distribution.maxOfOrNull { it.count } ?: 0
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item(key = "title") { Text(title, color = StatisticsCream, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(distribution, key = { it.value }) { item ->
            StatisticsPanel {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SingleDieView(Modifier.size(28.dp), DieValue.entries.first { it.value == item.value })
                    Box(Modifier.weight(1f).height(20.dp).clip(AppShapes.Inset).background(StatisticsOutline.copy(alpha = .2f))) {
                        val fraction = if (maxCount == 0) 0f else item.count.toFloat() / maxCount
                        Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(user?.appearanceColor ?: StatisticsBlue))
                    }
                    Text(item.count.toString(), color = StatisticsCream, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private data class HistogramState(val title: String, val distribution: List<DieValueDistribution>, val user: User? = null)

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun DiceStatisticsPreview() {
    com.alextos.thousand.application.theme.ThousandTheme {
        com.alextos.thousand.application.theme.AppBackground {
            Screen(Modifier, title = "Статистика кубиков", goBack = {}) { modifier ->
                DiceStatisticsContent(modifier, DiceStatisticsState(isLoading = false, totalDice = 669, averageDie = 3.6,
                    players = listOf(com.alextos.thousand.domain.usecase.statistics.PlayerWithDiceStatistics("1", "Легендарный Хомяк", 332, 3.6, emptyList())))) {}
            }
        }
    }
}
