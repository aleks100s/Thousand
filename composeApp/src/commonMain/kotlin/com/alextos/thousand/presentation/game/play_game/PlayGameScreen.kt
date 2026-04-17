package com.alextos.thousand.presentation.game.play_game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.presentation.game.components.GameHeaderView
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayGameScreen(
    onGoBack: () -> Unit,
    onScoreClick: (Game) -> Unit
) {
    val viewModel: PlayGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(PlayGameAction.LoadGame)
    }

    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = onGoBack,
        actions = {
            {
                state.game?.let {
                    TextButton(onClick = {
                        onScoreClick(it)
                    }) {
                        Text("Счет")
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
            Column(modifier = modifier.fillMaxSize()) {
                GameHeaderView(game, currentPlayer = game.players.firstOrNull())
            }
        }
    }
}