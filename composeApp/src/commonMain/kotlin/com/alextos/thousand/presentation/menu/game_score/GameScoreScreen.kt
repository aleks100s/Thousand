package com.alextos.thousand.presentation.menu.game_score

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.Game
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GameScoreScreen(
    onGoBack: () -> Unit,
    onResultsClick: (Game) -> Unit,
) {
    val viewModel: GameScoreViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(GameScoreAction.LoadGame)
    }

    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = onGoBack,
        actions = { actionColor ->
            {
                state.game?.takeIf { it.isFinished() }?.let { game ->
                    TextButton(
                        onClick = {
                            onResultsClick(game)
                        }
                    ) {
                        Text("Итоги", color = actionColor)
                    }
                }
            }
        }
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.turns.isEmpty()) {
                    item { Text("Бросков пока нет", modifier = Modifier.padding(24.dp)) }
                }
                items(state.turns, key = { it.id }) { turn ->
                    TurnHistoryCard(turn)
                }
            }
        }
    }
}
