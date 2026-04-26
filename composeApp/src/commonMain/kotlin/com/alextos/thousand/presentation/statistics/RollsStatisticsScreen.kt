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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import com.alextos.thousand.domain.usecase.statistics.PlayerWithRollStatistics
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RollsStatisticsScreen(
    goBack: () -> Unit,
) {
    val viewModel: RollsStatisticsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(RollsStatisticsAction.LoadStatistics)
    }

    Screen(
        modifier = Modifier,
        title = "Статистика бросков",
        goBack = goBack,
    ) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            RollsStatisticsContent(
                modifier = modifier,
                state = state,
            )
        }
    }
}

@Composable
private fun RollsStatisticsContent(
    modifier: Modifier,
    state: RollsStatisticsState,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            CommonRollsStatisticsCard(state)
        }

        item {
            if (state.players.isEmpty()) {
                EmptyRollsStatistics()
            } else {
                PlayersRollsStatisticsTable(state.players)
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CommonRollsStatisticsCard(
    state: RollsStatisticsState,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CommonRollsStatisticsItem(
                    title = "Броски",
                    value = state.totalRolls.toString(),
                )
                CommonRollsStatisticsItem(
                    title = "Средний",
                    value = state.averageRoll.toScoreText(),
                )
                CommonRollsStatisticsItem(
                    title = "Лучший",
                    value = state.bestRoll.toString(),
                )
                CommonRollsStatisticsItem(
                    title = "Средняя цепь",
                    value = state.averageRollChain.toScoreText(),
                )
                CommonRollsStatisticsItem(
                    title = "Лучшая цепь",
                    value = state.bestRollChain.toString(),
                )
            }
        }
    }
}

@Composable
private fun CommonRollsStatisticsItem(
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
private fun PlayersRollsStatisticsTable(
    players: List<PlayerWithRollStatistics>,
) {
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState),
    ) {
        PlayersRollsStatisticsTableHeader()

        players.forEach { player ->
            PlayerRollsStatisticsRow(player)
        }
    }
}

@Composable
private fun PlayersRollsStatisticsTableHeader() {
    RollsStatisticsTableRow(
        userName = "Игрок",
        rolls = "Броски",
        averageRoll = "Средний",
        bestRoll = "Лучший",
        averageRollChain = "Средняя цепь",
        bestRollChain = "Лучшая цепь",
        isHeader = true,
    )
    HorizontalDivider(modifier = Modifier.width(TABLE_WIDTH))
}

@Composable
private fun PlayerRollsStatisticsRow(
    player: PlayerWithRollStatistics,
) {
    RollsStatisticsTableRow(
        userName = player.userName,
        rolls = player.rolls.toString(),
        averageRoll = player.averageRoll.toScoreText(),
        bestRoll = player.bestRoll.toString(),
        averageRollChain = player.averageRollChain.toScoreText(),
        bestRollChain = player.bestRollChain.toString(),
        isHeader = false,
    )
}

@Composable
private fun RollsStatisticsTableRow(
    userName: String,
    rolls: String,
    averageRoll: String,
    bestRoll: String,
    averageRollChain: String,
    bestRollChain: String,
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
            .width(TABLE_WIDTH)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.width(USER_COLUMN_WIDTH),
            text = userName,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.width(METRIC_COLUMN_WIDTH),
            text = rolls,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.width(METRIC_COLUMN_WIDTH),
            text = averageRoll,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.width(METRIC_COLUMN_WIDTH),
            text = bestRoll,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.width(CHAIN_COLUMN_WIDTH),
            text = averageRollChain,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.width(CHAIN_COLUMN_WIDTH),
            text = bestRollChain,
            style = textStyle,
            fontWeight = fontWeight,
        )
    }
}

@Composable
private fun EmptyRollsStatistics() {
    Text(
        modifier = Modifier.padding(vertical = 24.dp),
        text = "Пока нет данных по броскам",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private val USER_COLUMN_WIDTH = 128.dp
private val METRIC_COLUMN_WIDTH = 92.dp
private val CHAIN_COLUMN_WIDTH = 120.dp
private val TABLE_WIDTH = 644.dp

private fun Double.toScoreText(): String {
    val rounded = (this * 10).roundToInt() / 10.0
    val text = rounded.toString()
    return if (text.endsWith(".0")) {
        text.dropLast(2)
    } else {
        text
    }
}
