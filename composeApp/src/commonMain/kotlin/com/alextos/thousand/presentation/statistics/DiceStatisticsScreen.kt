package com.alextos.thousand.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.usecase.statistics.DieValueDistribution
import com.alextos.thousand.domain.usecase.statistics.PlayerWithDiceStatistics
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiceStatisticsScreen(
    goBack: () -> Unit,
) {
    val viewModel: DiceStatisticsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var histogram by remember { mutableStateOf<HistogramState?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onAction(DiceStatisticsAction.LoadStatistics)
    }

    Screen(
        modifier = Modifier,
        title = "Статистика кубиков",
        goBack = goBack,
    ) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            DiceStatisticsContent(
                modifier = modifier,
                state = state,
                showHistogram = { histogram = it },
            )
        }
    }

    histogram?.let { histogramState ->
        ModalBottomSheet(
            onDismissRequest = {
                histogram = null
            },
        ) {
            DiceDistributionHistogram(
                title = histogramState.title,
                distribution = histogramState.distribution,
            )
        }
    }
}

@Composable
private fun DiceStatisticsContent(
    modifier: Modifier,
    state: DiceStatisticsState,
    showHistogram: (HistogramState) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            CommonDiceStatisticsCard(
                state = state,
                showHistogram = showHistogram,
            )
        }

        item {
            if (state.players.isEmpty()) {
                EmptyDiceStatistics()
            } else {
                PlayersDiceStatisticsTable(
                    players = state.players,
                    showHistogram = showHistogram,
                )
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CommonDiceStatisticsCard(
    state: DiceStatisticsState,
    showHistogram: (HistogramState) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Все игры",
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(
                    onClick = {
                        showHistogram(
                            HistogramState(
                                title = "Распределение кубиков",
                                distribution = state.distribution,
                            )
                        )
                    },
                ) {
                    Text("Гистограмма")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CommonDiceStatisticsItem(
                    title = "Кубики",
                    value = state.totalDice.toString(),
                )
                CommonDiceStatisticsItem(
                    title = "Средний",
                    value = state.averageDie.toScoreText(),
                )
            }
        }
    }
}

@Composable
private fun CommonDiceStatisticsItem(
    title: String,
    value: String,
) {
    Column(
        modifier = Modifier.width(132.dp),
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
private fun PlayersDiceStatisticsTable(
    players: List<PlayerWithDiceStatistics>,
    showHistogram: (HistogramState) -> Unit,
) {
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState),
    ) {
        PlayersDiceStatisticsTableHeader()

        players.forEach { player ->
            PlayerDiceStatisticsRow(
                player = player,
                showHistogram = showHistogram,
            )
        }
    }
}

@Composable
private fun PlayersDiceStatisticsTableHeader() {
    DiceStatisticsTableRow(
        userName = "Игрок",
        dice = "Кубики",
        averageDie = "Средний",
        isHeader = true,
        onHistogramClick = null,
    )
    HorizontalDivider(modifier = Modifier.width(TABLE_WIDTH))
}

@Composable
private fun PlayerDiceStatisticsRow(
    player: PlayerWithDiceStatistics,
    showHistogram: (HistogramState) -> Unit,
) {
    DiceStatisticsTableRow(
        userName = player.userName,
        dice = player.dice.toString(),
        averageDie = player.averageDie.toScoreText(),
        isHeader = false,
        onHistogramClick = {
            showHistogram(
                HistogramState(
                    title = "Распределение: ${player.userName}",
                    distribution = player.distribution,
                )
            )
        },
    )
}

@Composable
private fun DiceStatisticsTableRow(
    userName: String,
    dice: String,
    averageDie: String,
    isHeader: Boolean,
    onHistogramClick: (() -> Unit)?,
) {
    val textStyle = if (isHeader) {
        MaterialTheme.typography.labelLarge
    } else {
        MaterialTheme.typography.bodyMedium
    }
    val fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = Modifier
            .width(TABLE_WIDTH)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .width(USER_COLUMN_WIDTH)
                .then(
                    if (onHistogramClick == null) {
                        Modifier
                    } else {
                        Modifier.clickable(onClick = onHistogramClick)
                    }
                ),
            text = userName,
            style = textStyle,
            fontWeight = fontWeight,
            color = if (onHistogramClick == null) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
        Text(
            modifier = Modifier.width(METRIC_COLUMN_WIDTH),
            text = dice,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.width(METRIC_COLUMN_WIDTH),
            text = averageDie,
            style = textStyle,
            fontWeight = fontWeight,
        )
    }
}

@Composable
private fun DiceDistributionHistogram(
    title: String,
    distribution: List<DieValueDistribution>,
) {
    val maxCount = distribution.maxOfOrNull { it.count } ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )

        distribution.forEach { item ->
            DiceDistributionRow(
                item = item,
                maxCount = maxCount,
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DiceDistributionRow(
    item: DieValueDistribution,
    maxCount: Int,
) {
    val fraction = if (maxCount == 0) {
        0f
    } else {
        item.count.toFloat() / maxCount
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            modifier = Modifier.width(24.dp),
            text = item.value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }

        Text(
            modifier = Modifier.width(48.dp),
            text = item.count.toString(),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EmptyDiceStatistics() {
    Text(
        modifier = Modifier.padding(vertical = 24.dp),
        text = "Пока нет данных по кубикам",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private data class HistogramState(
    val title: String,
    val distribution: List<DieValueDistribution>,
)

private val USER_COLUMN_WIDTH = 128.dp
private val METRIC_COLUMN_WIDTH = 92.dp
private val TABLE_WIDTH = 312.dp

private fun Double.toScoreText(): String {
    val rounded = (this * 10).roundToInt() / 10.0
    val text = rounded.toString()
    return if (text.endsWith(".0")) {
        text.dropLast(2)
    } else {
        text
    }
}
