package com.alextos.thousand.presentation.game.create_game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.game.components.GameRulesView
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.casino_24px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGameScreen(
    openGame: (Long) -> Unit,
    goBack: () -> Unit
) {
    val viewModel: CreateGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(CreateGameAction.Initialize)
    }

    LaunchedEffect(state.createdGameId) {
        val createdGameId = state.createdGameId ?: return@LaunchedEffect
        openGame(createdGameId)
        viewModel.onAction(CreateGameAction.ConsumeCreatedGame)
    }

    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = goBack,
        floatingActionButton = {
            AnimatedVisibility(state.selectedUsers.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.onAction(CreateGameAction.CreateGame)
                    },
                    text = {
                        Text("Начать игру")
                    },
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.casino_24px),
                            contentDescription = "Начать игру"
                        )
                    }
                )
            }
        }
    ) { modifier ->
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    GameRulesView()
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    GameSettingsView(
                        state = state,
                        onAction = viewModel::onAction,
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "Выберите игроков",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                items(
                    items = state.users,
                    key = { user -> user.id },
                ) { user ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        trailingContent = {
                            Checkbox(
                                checked = state.selectedUsers.contains(user),
                                onCheckedChange = {
                                    viewModel.onAction(CreateGameAction.ToggleUserSelection(user))
                                }
                            )
                        },
                        modifier = Modifier
                            .clickable(onClick = {
                                viewModel.onAction(CreateGameAction.ToggleUserSelection(user))
                            })
                            .fillMaxWidth(),
                    )
                }

                item {
                    Button(
                        onClick = {
                            viewModel.onAction(CreateGameAction.ShowAddUserSheet)
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                    ) {
                        Text("Добавить нового")
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    if (state.isAddUserSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.onAction(CreateGameAction.HideAddUserSheet)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Новый пользователь",
                    style = MaterialTheme.typography.titleMedium,
                )

                OutlinedTextField(
                    value = state.newUserName,
                    onValueChange = { value ->
                        viewModel.onAction(CreateGameAction.UpdateNewUserName(value))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Имя пользователя")
                    },
                    singleLine = true,
                )

                Button(
                    onClick = {
                        viewModel.onAction(CreateGameAction.SaveNewUser)
                    },
                    enabled = state.newUserName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@Composable
private fun GameSettingsView(
    state: CreateGameState,
    onAction: (CreateGameAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "Настройки игры",
            style = MaterialTheme.typography.titleLarge,
        )
        GameSettingsItemView(
            text = "Уведомления во время игры",
            checked = state.isNotificationEnabled,
            onCheckedChange = {
                onAction(CreateGameAction.SetNotificationEnabled(it))
            },
        )
        GameSettingsItemView(
            text = "Бросать виртуальные кубики",
            checked = state.isVirtualDiceEnabled,
            onCheckedChange = {
                onAction(CreateGameAction.SetVirtualDiceEnabled(it))
            },
        )
        GameSettingsItemView(
            text = "Бросать кубики по тряске устройства",
            checked = state.isShakeEnabled,
            onCheckedChange = {
                onAction(CreateGameAction.SetShakeEnabled(it))
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GameSettingsItemView(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(checked.not())
            },
    )
}
