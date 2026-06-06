package com.alextos.thousand.presentation.menu.play_game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.presentation.components.GameMessageBubble
import com.alextos.thousand.presentation.components.GameMessagesOverlay
import com.alextos.thousand.presentation.components.GameView
import com.alextos.thousand.presentation.menu.game_rules.GameRulesContent
import com.alextos.thousand.presentation.menu.play_game.components.GameSettingsSheet
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.more_horiz_24px

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayGameScreen(
    onGoBack: () -> Unit,
    onScoreClick: (Game) -> Unit,
    onFinishGame: (Game) -> Unit
) {
    val viewModel: PlayGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val messages = remember { mutableStateListOf<GameMessageBubble>() }
    var nextMessageId by remember { mutableStateOf(0L) }
    var isRulesSheetVisible by remember { mutableStateOf(false) }
    var isSettingsSheetVisible by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PlayGameEvent.ShowMessage -> {
                    messages.add(
                        GameMessageBubble(
                            id = nextMessageId++,
                            text = event.message,
                            isReply = event.isReply
                        )
                    )
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
        actions = { actionColor ->
            {
                Box {
                    IconButton(
                        onClick = {
                            isMenuExpanded = true
                        },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.more_horiz_24px),
                            contentDescription = "Меню",
                            tint = actionColor,
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = {
                            isMenuExpanded = false
                        },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Правила")
                            },
                            onClick = {
                                isMenuExpanded = false
                                isRulesSheetVisible = true
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Настройки")
                            },
                            onClick = {
                                isMenuExpanded = false
                                isSettingsSheetVisible = true
                            },
                        )
                        state.gameState.game?.let { game ->
                            DropdownMenuItem(
                                text = {
                                    Text("Счет")
                                },
                                enabled = state.gameState.currentTurn.isEmpty(),
                                onClick = {
                                    isMenuExpanded = false
                                    onScoreClick(game)
                                },
                            )
                        }
                    }
                }
            }
        }
    ) { modifier ->
        Box(modifier = modifier.fillMaxSize()) {
            GameView(
                modifier = Modifier.fillMaxSize(),
                isManualInputEnabled = state.isManualInputEnabled,
                state = state.gameState,
                onAction = { action ->
                    viewModel.onAction(PlayGameAction.SendGameAction(action))
                },
                onFinishGame = {
                    viewModel.onAction(PlayGameAction.FinishGame)
                },
                onPlayerClick = null,
            )

            GameMessagesOverlay(
                messages = messages,
                onMessageDismiss = { message ->
                    messages.remove(message)
                },
            )
        }
    }

    if (isRulesSheetVisible) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                isRulesSheetVisible = false
            }
        ) {
            GameRulesContent(modifier = Modifier.fillMaxSize())
        }
    }

    if (isSettingsSheetVisible) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                isSettingsSheetVisible = false
            }
        ) {
            state.gameState.game?.let { game ->
                GameSettingsSheet(
                    settings = game.settings,
                    isNotificationEnabled = state.isNotificationEnabled,
                    onNotificationEnabledChange = { isEnabled ->
                        viewModel.onAction(
                            PlayGameAction.SetNotificationEnabled(
                                isEnabled
                            )
                        )
                    },
                )
            }
        }
    }
}
