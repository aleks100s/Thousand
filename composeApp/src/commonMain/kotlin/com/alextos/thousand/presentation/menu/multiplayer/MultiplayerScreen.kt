package com.alextos.thousand.presentation.menu.multiplayer

import com.alextos.thousand.presentation.components.appTextFieldColors
import com.alextos.thousand.application.theme.AppShapes
import com.alextos.thousand.presentation.components.AppButtonStyle
import com.alextos.thousand.presentation.components.AppButton
import com.alextos.thousand.presentation.components.AppBottomSheet as ModalBottomSheet
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.InfoCardView
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.presentation.components.GameHistoryCard
import com.alextos.thousand.presentation.components.GameStatusView
import com.alextos.thousand.presentation.components.AppDialogStatusBarEffect
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.language_24px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerScreen(
    goBack: () -> Unit,
    invitationCode: String? = null,
    invitationId: Long = 0,
    openCreateLobby: () -> Unit,
    openLobby: (String) -> Unit,
    openGame: (String) -> Unit,
) {
    val viewModel: MultiplayerViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MultiplayerEvent.OpenLobby -> openLobby(event.lobbyId)
            }
        }
    }

    LaunchedEffect(viewModel, invitationId) {
        invitationCode?.let { viewModel.onAction(MultiplayerAction.OpenInvitation(it, invitationId)) }
    }

    Screen(
        modifier = Modifier,
        constrainListWidth = true,
        title = "Мультиплеер",
        goBack = goBack,
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "hero", contentType = "hero") {
                MultiplayerHero(
                    state = state,
                    onAction = viewModel::onAction,
                    openCreateLobby = openCreateLobby,
                )
            }

            if (state.activeGames.isNotEmpty()) {
                item(key = "active-games-title", contentType = "section-title") {
                    Text(
                        "Активные игры",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            items(state.activeGames, key = { "game-${it.key}" }, contentType = { "remote-game" }) { game ->
                LobbyOrGameCard(
                    players = game.players.map { it.user },
                    host = game.host,
                    code = game.id.toString(),
                    isFinished = game.isFinished(),
                    onClick = {
                        openGame(game.key)
                    },
                    contextMenuText = "Удалить игру",
                    onContextMenuClick = {
                        viewModel.onAction(
                            MultiplayerAction.DeleteGame(
                                game.key
                            )
                        )
                    },
                )
            }

            if (state.lobbies.isNotEmpty()) {
                item(key = "lobbies-title", contentType = "section-title") {
                    Text(
                        "Активные лобби",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            items(state.lobbies, key = { "lobby-${it.key}" }, contentType = { "lobby" }) { lobby ->
                LobbyOrGameCard(
                    players = lobby.players,
                    host = lobby.host,
                    code = lobby.id,
                    onClick = {
                        openLobby(lobby.key)
                    },
                    contextMenuText = "Покинуть лобби",
                    onContextMenuClick = {
                        viewModel.onAction(
                            MultiplayerAction.DisconnectFromLobby(
                                lobby.key
                            )
                        )
                    },
                )
            }

            if (state.finishedGames.isNotEmpty()) {
                item(key = "finished-games-title", contentType = "section-title") {
                    Text(
                        "Завершенные игры",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            items(state.finishedGames, key = { "game-${it.key}" }, contentType = { "remote-game" }) { game ->
                LobbyOrGameCard(
                    players = game.players.map { it.user },
                    host = game.host,
                    code = game.id.toString(),
                    isFinished = game.isFinished(),
                    onClick = {
                        openGame(game.key)
                    },
                    contextMenuText = "Удалить игру",
                    onContextMenuClick = {
                        viewModel.onAction(
                            MultiplayerAction.DeleteGame(
                                game.key
                            )
                        )
                    },
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (state.isJoinLobbySheetVisible && !state.isJoiningLobby) {
        JoinLobbySheet(
            state = state,
            onAction = viewModel::onAction,
        )
    }

    if (state.isConnecting || state.isJoiningLobby) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        ) {
            AppDialogStatusBarEffect()
            Surface(shape = MaterialTheme.shapes.large) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(if (state.isJoiningLobby) "Подключение к лобби…" else "Подключение к мультиплееру…")
                }
            }
        }
    }
}

@Composable
private fun LobbyOrGameCard(
    players: List<User>,
    host: String,
    code: String,
    isFinished: Boolean? = null,
    onClick: () -> Unit,
    contextMenuText: String,
    onContextMenuClick: () -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val hostName = players
        .firstOrNull { player -> player.id == host }
        ?.name
        ?: "Без имени"

    Box {
        GameHistoryCard(
            title = "Игра $code",
            subtitle = "Хост: $hostName",
            supportingText = "Игроков: ${players.count()}",
            accentColor = if (isFinished == true) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.secondary
            },
            modifier = Modifier
                .fillMaxWidth(),
            onClick = onClick,
            onLongClick = {
                isMenuExpanded = true
            },
            trailingContent = {
                isFinished?.let {
                    GameStatusView(it, finishedAt = null)
                } ?: Text(
                    text = "Подключиться",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        )

        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = {
                isMenuExpanded = false
            },
        ) {
            DropdownMenuItem(
                text = {
                    Text(contextMenuText)
                },
                colors = MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.error,
                ),
                onClick = {
                    isMenuExpanded = false
                    onContextMenuClick()
                },
            )
        }
    }
}

@Composable
private fun MultiplayerHero(
    state: MultiplayerState,
    onAction: (MultiplayerAction) -> Unit,
    openCreateLobby: () -> Unit,
) {
    InfoCardView(
        icon = Res.drawable.language_24px,
        title = "Играйте с друзьями по сети",
        text = "Мультиплеер поможет собрать игроков в одну партию. Хост создает игру и делится кодом подключения с другими",
    ) {
        MultiplayerActions(
            state = state,
            onAction = onAction,
            openCreateLobby = openCreateLobby,
        )
    }
}

@Composable
private fun MultiplayerActions(
    state: MultiplayerState,
    onAction: (MultiplayerAction) -> Unit,
    openCreateLobby: () -> Unit,
) {
    if (state.isAuthorized) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppButton(style = AppButtonStyle.Secondary,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onAction(MultiplayerAction.ShowJoinLobbySheet)
                },
            ) {
                Text("Присоединиться")
            }

            AppButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = openCreateLobby,
            ) {
                Text("Создать игру")
            }
        }
    } else if (state.connectionError != null) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(state.connectionError, color = MaterialTheme.colorScheme.error)
            AppButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAction(MultiplayerAction.RetryConnection) },
            ) {
                Text("Повторить")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinLobbySheet(
    state: MultiplayerState,
    onAction: (MultiplayerAction) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var lobbyId by rememberSaveable { mutableStateOf(state.lobbyId) }
    val canJoinLobby = lobbyId.trim().isNotBlank()

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = {
            onAction(MultiplayerAction.HideJoinLobbySheet)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Подключиться к лобби",
                style = MaterialTheme.typography.titleMedium,
            )

            Text(text = "Введите код из 5 латинских букв и цифр")

            OutlinedTextField(
                colors = appTextFieldColors(),
                shape = AppShapes.Row,
                value = lobbyId,
                onValueChange = { value ->
                    lobbyId = value
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Код лобби")
                },
                placeholder = {
                    Text("A7K2M")
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Ascii,
                ),
                singleLine = true,
            )

            if (state.lobbyError != null) {
                Text(text = state.lobbyError, color = MaterialTheme.colorScheme.error)
            }

            AppButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = canJoinLobby,
                onClick = {
                    keyboardController?.hide()
                    onAction(MultiplayerAction.UpdateLobbyId(lobbyId))
                    onAction(MultiplayerAction.JoinLobby)
                },
            ) {
                Text("Подключиться")
            }
        }
    }
}
