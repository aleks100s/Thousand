package com.alextos.thousand.presentation.multiplayer.multiplayer_game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.RemoteUserInfo
import com.alextos.thousand.presentation.components.GameMessageBubble
import com.alextos.thousand.presentation.components.GameMessagesOverlay
import com.alextos.thousand.presentation.components.GameView
import com.alextos.thousand.presentation.menu.play_game.components.GameSettingsSheet
import com.alextos.thousand.presentation.menu.game_rules.GameRulesContent
import com.alextos.thousand.presentation.multiplayer.player_profile.PlayerProfileView
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.more_horiz_24px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerGameScreen(
    goBack: () -> Unit,
) {
    val viewModel: MultiplayerGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val messages = remember { mutableStateListOf<GameMessageBubble>() }
    var nextMessageId by remember { mutableStateOf(0L) }
    var isDeleteGameSheetVisible by remember { mutableStateOf(false) }
    var isRulesSheetVisible by remember { mutableStateOf(false) }
    var isSettingsSheetVisible by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedUserInfo by remember { mutableStateOf<RemoteUserInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showWinSheet by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                MultiplayerGameEvent.GameDeleted -> goBack()
                is MultiplayerGameEvent.ShowMessage -> {
                    messages.add(
                        GameMessageBubble(
                            id = nextMessageId++,
                            text = event.message,
                            isReply = false
                        )
                    )
                }
                is MultiplayerGameEvent.Error -> {
                    errorMessage = event.message
                }
            }
        }
    }

    if (state.error != null) {
        ModalBottomSheet(
            onDismissRequest = goBack
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.error ?: "")

                Button(onClick = goBack) {
                    Text("Выйти в меню")
                }
            }
        }
    }

    errorMessage?.let { message ->
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                errorMessage = null
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        errorMessage = null
                    },
                ) {
                    Text("Ok")
                }
            }
        }
    }

    Screen(
        modifier = Modifier,
        title = "Игра ${state.gameCode}",
        goBack = goBack,
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
                        DropdownMenuItem(
                            text = {
                                Text(if (state.isHost) "Удалить игру" else "Покинуть игру")
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error,
                            ),
                            onClick = {
                                isMenuExpanded = false
                                isDeleteGameSheetVisible = true
                            },
                        )
                    }
                }
            }
        },
    ) { modifier ->
        GameView(
            modifier = modifier.fillMaxSize(),
            isManualInputEnabled = false,
            state = state.gameState,
            onAction = { action ->
                viewModel.onAction(MultiplayerGameAction.SendGameAction(action))
            },
            onFinishGame = {
                showWinSheet = true
            },
            onPlayerClick = { player ->
                selectedUserInfo = state.usersInfo[player.user.id]
            },
        )

        GameMessagesOverlay(
            messages = messages,
            onMessageDismiss = { message ->
                messages.remove(message)
            },
        )
    }

    if (isDeleteGameSheetVisible) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                isDeleteGameSheetVisible = false
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Удалить эту игру?",
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = "Это действие нельзя отменить. Игра будет удалена для всех участников.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                    onClick = {
                        isDeleteGameSheetVisible = false
                        viewModel.onAction(MultiplayerGameAction.DeleteGame)
                    },
                ) {
                    Text("Удалить игру")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isDeleteGameSheetVisible = false
                    },
                ) {
                    Text("Отмена")
                }
            }
        }
    }

    if (isRulesSheetVisible) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                isRulesSheetVisible = false
            },
        ) {
            GameRulesContent(modifier = Modifier.fillMaxSize())
        }
    }

    if (isSettingsSheetVisible) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                isSettingsSheetVisible = false
            },
        ) {
            state.gameState.game?.settings?.let { settings ->
                GameSettingsSheet(
                    settings = settings,
                    isNotificationEnabled = state.isNotificationEnabled,
                    onNotificationEnabledChange = {
                        viewModel.onAction(MultiplayerGameAction.ToggleNotifications(it))
                    },
                )
            }
        }
    }

    if (showWinSheet) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                showWinSheet = false
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Победа!",
                    style = MaterialTheme.typography.titleLarge,
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showWinSheet = false
                        viewModel.onAction(MultiplayerGameAction.Rematch)
                    },
                ) {
                    Text("Сыграть заново")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showWinSheet = false
                        goBack()
                    },
                ) {
                    Text("Выйти")
                }
            }
        }
    }

    selectedUserInfo?.let { userInfo ->
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            onDismissRequest = {
                selectedUserInfo = null
            },
        ) {
            PlayerProfileView(
                username = userInfo.name,
                gameCount = userInfo.gameCount,
                winCount = userInfo.winCount,
                rating = userInfo.rating,
            )
        }
    }
}
