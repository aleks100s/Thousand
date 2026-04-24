package com.alextos.thousand.presentation.game.create_game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.GameConstants.BARREL_1
import com.alextos.thousand.domain.GameConstants.BARREL_2
import com.alextos.thousand.domain.GameConstants.BARREL_3
import com.alextos.thousand.domain.GameConstants.BOLT_FINE
import com.alextos.thousand.domain.GameConstants.OVERTAKE_FINE
import com.alextos.thousand.domain.GameConstants.STARTING_LIMIT
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.add_24px
import thousand.composeapp.generated.resources.casino_24px
import thousand.composeapp.generated.resources.mobile_hand_24px
import thousand.composeapp.generated.resources.mobile_vibrate_24px
import thousand.composeapp.generated.resources.notifications_24px
import thousand.composeapp.generated.resources.notifications_off_24px
import thousand.composeapp.generated.resources.sports_esports_24px

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateGameEvent.OpenGame -> openGame(event.gameId)
            }
        }
    }

    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = {
            when (state.step) {
                CreateGameStep.Players -> goBack()
                CreateGameStep.Settings -> viewModel.onAction(CreateGameAction.OpenPlayersStep)
            }
        },
        actions = {
            {
                if (state.step == CreateGameStep.Players) {
                    IconButton(
                        onClick = {
                            viewModel.onAction(CreateGameAction.ShowAddUserSheet)
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.add_24px),
                            contentDescription = "Добавить игрока"
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (state.step) {
                CreateGameStep.Players -> {
                    AnimatedVisibility(state.selectedUsers.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = {
                                viewModel.onAction(CreateGameAction.OpenSettingsStep)
                            },
                            shape = FloatingActionButtonDefaults.largeShape
                        ) {
                            Text("Далее")
                        }
                    }
                }
                CreateGameStep.Settings -> {
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
                when (state.step) {
                    CreateGameStep.Players -> {
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
                    }
                    CreateGameStep.Settings -> {
                        item {
                            GameSettingsView(
                                state = state,
                                onAction = viewModel::onAction,
                            )
                        }
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
        val text = if (state.isVirtualDiceEnabled) {
            "В игровом режиме вам не понадобятся настоящие кубики - вы сможете играть прямо на телефоне."
        } else {
            "Режим ассистента позволяет вести счет реальной игры в приложении"
        }
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(if (state.isVirtualDiceEnabled) Res.drawable.sports_esports_24px else Res.drawable.casino_24px),
                contentDescription = null
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall
            )
        }

        SingleChoiceSegmentedButtonRow(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
            listOf(true, false).forEachIndexed { index, isEnabled ->
                SegmentedButton(
                    selected = state.isVirtualDiceEnabled == isEnabled,
                    onClick = {
                        onAction(CreateGameAction.SetVirtualDiceEnabled(isEnabled))
                    },
                    label = {
                        Text(if (isEnabled) "Игровой режим" else "Режим ассистента")
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = 2
                    )
                )
            }
        }

        AnimatedVisibility(state.isVirtualDiceEnabled) {
            GameSettingsItemView(
                text = "Бросать кубики по тряске устройства",
                resource = if (state.isShakeEnabled) Res.drawable.mobile_vibrate_24px else Res.drawable.mobile_hand_24px,
                checked = state.isShakeEnabled,
                onCheckedChange = {
                    onAction(CreateGameAction.SetShakeEnabled(it))
                },
            )
        }

        GameSettingsItemView(
            text = "Уведомления во время игры",
            resource = if (state.isNotificationEnabled) Res.drawable.notifications_24px else Res.drawable.notifications_off_24px,
            checked = state.isNotificationEnabled,
            onCheckedChange = {
                onAction(CreateGameAction.SetNotificationEnabled(it))
            },
        )

        GameSettingsItemView(
            text = "Открытие игры с $STARTING_LIMIT очков",
            resource = Res.drawable.casino_24px,
            checked = state.hasStartLimit,
            onCheckedChange = {
                onAction(CreateGameAction.SetHasStartLimit(it))
            },
        )

        GameSettingsItemView(
            text = "Первая бочка $BARREL_1",
            resource = Res.drawable.casino_24px,
            checked = state.isBarrel1Active,
            onCheckedChange = {
                onAction(CreateGameAction.SetBarrel1Active(it))
            },
        )

        GameSettingsItemView(
            text = "Вторая бочка $BARREL_2",
            resource = Res.drawable.casino_24px,
            checked = state.isBarrel2Active,
            onCheckedChange = {
                onAction(CreateGameAction.SetBarrel2Active(it))
            },
        )

        GameSettingsItemView(
            text = "Третья бочка $BARREL_3",
            resource = Res.drawable.casino_24px,
            checked = state.isBarrel3Active,
            onCheckedChange = {
                onAction(CreateGameAction.SetBarrel3Active(it))
            },
        )

        GameSettingsItemView(
            text = "Штраф $BOLT_FINE очков за 3 болта",
            resource = Res.drawable.casino_24px,
            checked = state.isTripleBoltFineActive,
            onCheckedChange = {
                onAction(CreateGameAction.SetTripleBoltFineActive(it))
            },
        )

        GameSettingsItemView(
            text = "Штраф за обгон $OVERTAKE_FINE очков",
            resource = Res.drawable.casino_24px,
            checked = state.isOvertakeFineActive,
            onCheckedChange = {
                onAction(CreateGameAction.SetOvertakeFineActive(it))
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GameSettingsItemView(
    text: String,
    resource: DrawableResource,
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
        leadingContent = {
            Icon(
                painter = painterResource(resource),
                contentDescription = text
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
