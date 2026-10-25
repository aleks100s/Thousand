package com.alextos.thousand.presentation.menu.game_score

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GameScoreScreen(
    onGoBack: () -> Unit,
) {
    val viewModel: GameScoreViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(GameScoreAction.LoadGame)
    }

    GameScoreContent(state, onGoBack)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun GameScoreContent(state: GameScoreState, onGoBack: () -> Unit) {
    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = onGoBack,
    ) { modifier ->
        val game = state.game
        if (game == null) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.showSummary) {
                    item(key = "chart", contentType = "chart") {
                        Column { ScoreChartCard(series = state.scoreSeries); Spacer(Modifier.height(12.dp)) }
                    }
                    items(state.playerStatistics, key = { "statistics-${it.userId}" }, contentType = { "player-statistics" }) { player ->
                        Column { PlayerStatisticsCard(statistics = player); Spacer(Modifier.height(12.dp)) }
                    }
                    item(key = "history-title", contentType = "section-title") {
                        Text("История бросков", modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    }
                }
                if (state.turns.isEmpty()) {
                    item(key = "empty", contentType = "empty") { Text("Бросков пока нет", modifier = Modifier.padding(24.dp)) }
                }
                items(state.turnRows, key = { it.key }, contentType = { it::class }) { row ->
                    TurnHistoryLazyRow(row)
                }
            }
        }
    }
}
