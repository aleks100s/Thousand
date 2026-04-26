package com.alextos.thousand.presentation.game.play_game

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.presentation.game.components.GameRulesView
import com.alextos.thousand.presentation.game.components.GameView
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayGameScreen(
    onGoBack: () -> Unit,
    onScoreClick: (Game) -> Unit,
    onFinishGame: (Game) -> Unit
) {
    val viewModel: PlayGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var isRulesSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onAction(PlayGameAction.LoadGame)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PlayGameEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message, withDismissAction = true)
                }
                is PlayGameEvent.FinishGame -> {
                    onFinishGame(event.game)
                }
            }
        }
    }
    
    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = onGoBack,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        actions = {
            {
                TextButton(
                    onClick = {
                        isRulesSheetVisible = true
                    }
                ) {
                    Text("Правила")
                }

                state.gameState.game?.let {
                    TextButton(onClick = {
                        onScoreClick(it)
                    }) {
                        Text("Счет")
                    }
                }
            }
        }
    ) { modifier ->
        GameView(
            modifier = modifier,
            isManualInputEnabled = state.isManualInputEnabled,
            state = state.gameState,
            onAction = { action ->
                viewModel.onAction(PlayGameAction.SendGameAction(action))
            },
            onFinishGame = {
                viewModel.onAction(PlayGameAction.FinishGame)
            }
        )
    }

    if (isRulesSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                isRulesSheetVisible = false
            }
        ) {
            state.gameState.game?.let { game ->
                GameRulesView(game = game)
            }
        }
    }
}