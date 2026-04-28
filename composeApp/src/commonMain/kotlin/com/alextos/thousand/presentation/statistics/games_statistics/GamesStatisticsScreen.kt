package com.alextos.thousand.presentation.statistics.games_statistics

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
import com.alextos.thousand.domain.usecase.statistics.PlayerWithStatistics
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GamesStatisticsScreen(
    goBack: () -> Unit,
) {
    val viewModel: GamesStatisticsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(GamesStatisticsAction.LoadStatistics)
    }

    Screen(
        modifier = Modifier,
        title = "Статистика игр",
        goBack = goBack,
    ) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            GamesStatisticsContent(
                modifier = modifier,
                state = state,
            )
        }
    }
}

@Composable
private fun GamesStatisticsContent(
    modifier: Modifier,
    state: GamesStatisticsState,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FinishedGamesCard(state.finishedGamesCount)
        }

        item {
            PlayersStatisticsTableHeader()
        }

        if (state.players.isEmpty()) {
            item {
                EmptyStatistics()
            }
        } else {
            items(
                items = state.players,
                key = { player -> player.userId },
            ) { player ->
                PlayerStatisticsRow(player)
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FinishedGamesCard(
    finishedGamesCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Завершенные игры",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = finishedGamesCount.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun PlayersStatisticsTableHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatisticsTableRow(
            userName = "Игрок",
            games = "Игры",
            wins = "Победы",
            losses = "Поражения",
            isHeader = true,
        )
        HorizontalDivider()
    }
}

@Composable
private fun PlayerStatisticsRow(
    player: PlayerWithStatistics,
) {
    StatisticsTableRow(
        userName = player.userName,
        games = player.games.toString(),
        wins = player.wins.toString(),
        losses = player.losses.toString(),
        isHeader = false,
    )
}

@Composable
private fun StatisticsTableRow(
    userName: String,
    games: String,
    wins: String,
    losses: String,
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
            text = games,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.weight(0.8f),
            text = wins,
            style = textStyle,
            fontWeight = fontWeight,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = losses,
            style = textStyle,
            fontWeight = fontWeight,
        )
    }
}

@Composable
private fun EmptyStatistics() {
    Text(
        modifier = Modifier.padding(vertical = 24.dp),
        text = "Пока нет данных по игрокам",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
