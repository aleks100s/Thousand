package com.alextos.thousand.presentation.multiplayer

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.InfoCardView
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.User
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.diversity_3_24px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerScreen(
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

    Screen(
        modifier = Modifier,
        title = "Мультиплеер",
        actions = {
            {
                state.username?.let {
                    TextButton(onClick = {
                        viewModel.onAction(MultiplayerAction.ShowLogoutSheet)
                    }) {
                        Text(it)
                    }
                }
            }
        }
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                MultiplayerHero(
                    state = state,
                    onAction = viewModel::onAction,
                    openCreateLobby = openCreateLobby,
                )
            }

            if (state.games.isNotEmpty()) {
                item {
                    Text(
                        "Активные игры",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            items(state.games) { game ->
                LobbyOrGameCard(
                    players = game.players.map { it.user },
                    host = "",
                    code = game.id.toString(),
                    onTap = {
                        openGame("")
                    },
                )
            }

            if (state.lobbies.isNotEmpty()) {
                item {
                    Text(
                        "Активные лобби",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            items(state.lobbies) { lobby ->
                LobbyOrGameCard(
                    players = lobby.players,
                    host = lobby.host,
                    code = lobby.id,
                    onTap = {
                        openLobby(lobby.id)
                    },
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (state.isLoginSheetVisible) {
        LoginSheet(
            state = state,
            onAction = viewModel::onAction,
        )
    }

    if (state.isJoinLobbySheetVisible) {
        JoinLobbySheet(
            state = state,
            onAction = viewModel::onAction,
        )
    }

    if (state.isLogoutSheetVisible) {
        LogoutSheet(
            onSignOut = {
                viewModel.onAction(MultiplayerAction.SignOut)
            },
            onDismiss = {
                viewModel.onAction(MultiplayerAction.HideLogoutSheet)
            }
        )
    }
}

@Composable
private fun LobbyOrGameCard(
    players: List<User>,
    host: String,
    code: String,
    onTap: () -> Unit,
) {
    val hostName = players
        .firstOrNull { player -> player.id == host }
        ?.name
        ?: "Без имени"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        onClick = {
            onTap()
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Игра $code",
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = "Подключиться",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Хост: $hostName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = "Игроков: ${players.count()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
        icon = Res.drawable.diversity_3_24px,
        title = "Играйте с друзьями",
        text = "Мультиплеер поможет собрать игроков в одну партию: один создает игру, остальные подключаются, а приложение синхронизирует ход, броски, эффекты и итоговый счет.",
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
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onAction(MultiplayerAction.ShowJoinLobbySheet)
                },
            ) {
                Text("Присоединиться")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = openCreateLobby,
            ) {
                Text("Создать игру")
            }
        }
    } else {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onAction(MultiplayerAction.ShowLoginSheet)
            },
        ) {
            Text("Авторизоваться")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinLobbySheet(
    state: MultiplayerState,
    onAction: (MultiplayerAction) -> Unit,
) {
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

            OutlinedTextField(
                value = state.lobbyId,
                onValueChange = { value ->
                    onAction(MultiplayerAction.UpdateLobbyId(value))
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("ID лобби")
                },
                singleLine = true,
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canJoinLobby,
                onClick = {
                    onAction(MultiplayerAction.JoinLobby)
                },
            ) {
                Text("Подключиться")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginSheet(
    state: MultiplayerState,
    onAction: (MultiplayerAction) -> Unit,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = {
            onAction(MultiplayerAction.HideLoginSheet)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Вход в мультиплеер",
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = { value ->
                    onAction(MultiplayerAction.UpdateEmail(value))
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Email")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = { value ->
                    onAction(MultiplayerAction.UpdatePassword(value))
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Пароль")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            if (state.error != null) {
                Text(text = state.error, color = MaterialTheme.colorScheme.error)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = state.loginSheetButtonsEnabled,
                    onClick = {
                        onAction(MultiplayerAction.SignUp)
                    },
                ) {
                    if (state.isSignUpInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Регистрация")
                    }
                }

                Button(
                    modifier = Modifier.weight(1f),
                    enabled = state.loginSheetButtonsEnabled,
                    onClick = {
                        onAction(MultiplayerAction.LogIn)
                    },
                ) {
                    if (state.isLoginInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Вход")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogoutSheet(
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = {
            onDismiss()
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Вы действительно хотите выйти из аккаунта?")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onSignOut()
                    },
                ) {
                    Text("Выйти")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismiss()
                    },
                ) {
                    Text("Отмена")
                }
            }
        }
    }
}
