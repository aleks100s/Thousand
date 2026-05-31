package com.alextos.thousand.presentation.menu.statistics.events_statistics

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
import com.alextos.thousand.domain.usecase.statistics.PlayerWithEventsStatistics
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EventsStatisticsScreen(
    goBack: () -> Unit,
) {
    val viewModel: EventsStatisticsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(EventsStatisticsAction.LoadStatistics)
    }

    Screen(
        modifier = Modifier,
        title = "Статистика событий",
        goBack = goBack,
    ) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            EventsStatisticsContent(
                modifier = modifier,
                state = state,
            )
        }
    }
}

@Composable
private fun EventsStatisticsContent(
    modifier: Modifier,
    state: EventsStatisticsState,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            CommonEventsStatisticsCard(
                state
            )
        }

        item {
            PlayersEventsStatisticsTableHeader()
        }

        if (state.players.isEmpty()) {
            item {
                EmptyEventsStatistics()
            }
        } else {
            items(
                items = state.players,
                key = { player -> player.userId },
            ) { player ->
                PlayerEventsStatisticsRow(
                    player
                )
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CommonEventsStatisticsCard(
    state: EventsStatisticsState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Все события",
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                EventCountItem(
                    modifier = Modifier.weight(1f),
                    title = "Упал с 555",
                    count = state.pitFalls,
                )
                EventCountItem(
                    modifier = Modifier.weight(1f),
                    title = "Обогнал другого",
                    count = state.overtakes,
                )
                EventCountItem(
                    modifier = Modifier.weight(1f),
                    title = "Три болта",
                    count = state.tripleBolts,
                )
            }
        }
    }
}

@Composable
private fun EventCountItem(
    modifier: Modifier,
    title: String,
    count: Int,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
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
private fun PlayersEventsStatisticsTableHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        EventsStatisticsTableRow(
            userName = "Игрок",
            pitFalls = "555",
            overtakes = "Обгоны",
            tripleBolts = "Болты",
            isHeader = true,
        )
        HorizontalDivider()
    }
}

@Composable
private fun PlayerEventsStatisticsRow(
    player: PlayerWithEventsStatistics,
) {
    EventsStatisticsTableRow(
        userName = player.userName,
        pitFalls = player.pitFalls.toString(),
        overtakes = player.overtakes.toString(),
        tripleBolts = player.tripleBolts.toString(),
        isHeader = false,
    )
}

@Composable
private fun EventsStatisticsTableRow(
    userName: String,
    pitFalls: String,
    overtakes: String,
    tripleBolts: String,
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
            modifier = Modifier.weight(0.6f),
            text = pitFalls,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.weight(0.9f),
            text = overtakes,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.weight(0.7f),
            text = tripleBolts,
            style = textStyle,
            fontWeight = fontWeight,
        )
    }
}

@Composable
private fun EmptyEventsStatistics() {
    Text(
        modifier = Modifier.padding(vertical = 24.dp),
        text = "Пока нет событий для статистики",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
