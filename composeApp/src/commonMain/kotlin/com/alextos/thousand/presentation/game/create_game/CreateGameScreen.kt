package com.alextos.thousand.presentation.game.create_game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.GameConstants.BARREL_1
import com.alextos.thousand.domain.GameConstants.BARREL_2
import com.alextos.thousand.domain.GameConstants.BARREL_3
import com.alextos.thousand.domain.GameConstants.BOLT_FINE
import com.alextos.thousand.domain.GameConstants.OVERTAKE_FINE
import com.alextos.thousand.domain.GameConstants.STARTING_LIMIT
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.add_24px
import thousand.composeapp.generated.resources.casino_24px
import thousand.composeapp.generated.resources.info_24px
import thousand.composeapp.generated.resources.mobile_hand_24px
import thousand.composeapp.generated.resources.mobile_vibrate_24px
import thousand.composeapp.generated.resources.notifications_24px
import thousand.composeapp.generated.resources.notifications_off_24px
import thousand.composeapp.generated.resources.person_add_24px
import thousand.composeapp.generated.resources.robot_24px
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
                        ExtendedFloatingActionButton(
                            onClick = {
                                viewModel.onAction(CreateGameAction.OpenSettingsStep)
                            }
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
            when (state.step) {
                CreateGameStep.Players -> {
                    PlayersGrid(
                        modifier = Modifier.fillMaxSize(),
                        users = state.users,
                        selectedUsers = state.selectedUsers,
                        onAddUser = {
                            viewModel.onAction(CreateGameAction.ShowAddUserSheet)
                        },
                        onAddBot = {
                            viewModel.onAction(CreateGameAction.ShowAddBotSheet)
                        },
                        onToggleUser = { user ->
                            viewModel.onAction(CreateGameAction.ToggleUserSelection(user))
                        },
                    )
                }
                CreateGameStep.Settings -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            GameSettingsView(
                                state = state,
                                onAction = viewModel::onAction,
                            )
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (state.isAddUserSheetVisible) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
                    text = if (state.newUserKind == UserKind.Bot) {
                        "Новый бот"
                    } else {
                        "Новый пользователь"
                    },
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
private fun PlayersGrid(
    modifier: Modifier,
    users: List<User>,
    selectedUsers: Set<User>,
    onAddUser: () -> Unit,
    onAddBot: () -> Unit,
    onToggleUser: (User) -> Unit,
) {
    BoxWithConstraints(modifier) {
        val columns = if (maxHeight >= maxWidth) {
            PORTRAIT_PLAYER_COLUMNS
        } else {
            LANDSCAPE_PLAYER_COLUMNS
        }

        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "add_user") {
                AddUserCard(onClick = onAddUser)
            }

            item(key = "add_bot") {
                AddBotCard(onClick = onAddBot)
            }

            items(
                items = users,
                key = { user -> user.id },
            ) { user ->
                PlayerCard(
                    user = user,
                    isSelected = selectedUsers.contains(user),
                    onClick = {
                        onToggleUser(user)
                    },
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AddUserCard(
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(PLAYER_CARD_HEIGHT)
            .clickable(onClick = onClick),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.person_add_24px),
                    contentDescription = "Добавить игрока",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Добавить игрока",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddBotCard(
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(PLAYER_CARD_HEIGHT)
            .clickable(onClick = onClick),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.robot_24px),
                    contentDescription = "Добавить бота",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Добавить бота",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PlayerCard(
    user: User,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(PLAYER_CARD_HEIGHT)
            .clickable(onClick = onClick),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            Checkbox(
                modifier = Modifier
                    .offset(x = 8.dp, y = (-8).dp)
                    .align(Alignment.TopEnd),
                checked = isSelected,
                onCheckedChange = {
                    onClick()
                },
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box {
                    if (user.kind == UserKind.Bot) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .size(56.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.robot_24px),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    } else {
                        UserAvatar(user)
                    }
                }

                Text(
                    text = user.name,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun UserAvatar(
    user: User,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = user.name.initial(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

private fun String.initial(): String {
    return trim()
        .firstOrNull()
        ?.uppercaseChar()
        ?.toString()
        ?: "?"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameSettingsView(
    state: CreateGameState,
    onAction: (CreateGameAction) -> Unit,
) {
    var settingInfo by remember { mutableStateOf<GameSettingInfo?>(null) }

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
            resource = Res.drawable.info_24px,
            checked = state.hasStartLimit,
            onCheckedChange = {
                onAction(CreateGameAction.SetHasStartLimit(it))
            },
            onInfoClick = {
                settingInfo = GameSettingInfo(
                    title = "Открытие игры",
                    description = "Игрок начинает набирать очки только после хода на $STARTING_LIMIT очков или больше.",
                )
            },
        )

        GameSettingsItemView(
            text = "Первая бочка $BARREL_1",
            resource = Res.drawable.info_24px,
            checked = state.isBarrel1Active,
            onCheckedChange = {
                onAction(CreateGameAction.SetBarrel1Active(it))
            },
            onInfoClick = {
                settingInfo = GameSettingInfo(
                    title = "Первая бочка",
                    description = "Если счет игрока остается в диапазоне $BARREL_1, ход не засчитывается и игрок получает болт.",
                )
            },
        )

        GameSettingsItemView(
            text = "Вторая бочка $BARREL_2",
            resource = Res.drawable.info_24px,
            checked = state.isBarrel2Active,
            onCheckedChange = {
                onAction(CreateGameAction.SetBarrel2Active(it))
            },
            onInfoClick = {
                settingInfo = GameSettingInfo(
                    title = "Вторая бочка",
                    description = "Если счет игрока остается в диапазоне $BARREL_2, ход не засчитывается и игрок получает болт.",
                )
            },
        )

        GameSettingsItemView(
            text = "Третья бочка $BARREL_3",
            resource = Res.drawable.info_24px,
            checked = state.isBarrel3Active,
            onCheckedChange = {
                onAction(CreateGameAction.SetBarrel3Active(it))
            },
            onInfoClick = {
                settingInfo = GameSettingInfo(
                    title = "Третья бочка",
                    description = "Если счет игрока остается в диапазоне $BARREL_3, ход не засчитывается и игрок получает болт.",
                )
            },
        )

        GameSettingsItemView(
            text = "Штраф $BOLT_FINE очков за 3 болта",
            resource = Res.drawable.info_24px,
            checked = state.isTripleBoltFineActive,
            onCheckedChange = {
                onAction(CreateGameAction.SetTripleBoltFineActive(it))
            },
            onInfoClick = {
                settingInfo = GameSettingInfo(
                    title = "Штраф за 3 болта",
                    description = "Если игрок получает третий болт подряд, его счет уменьшается на $BOLT_FINE очков.",
                )
            },
        )

        GameSettingsItemView(
            text = "Штраф за обгон $OVERTAKE_FINE очков",
            resource = Res.drawable.info_24px,
            checked = state.isOvertakeFineActive,
            onCheckedChange = {
                onAction(CreateGameAction.SetOvertakeFineActive(it))
            },
            onInfoClick = {
                settingInfo = GameSettingInfo(
                    title = "Штраф за обгон",
                    description = "Когда игрок обгоняет соперника по счету, соперник теряет $OVERTAKE_FINE очков.",
                )
            },
        )
    }

    settingInfo?.let { info ->
        ModalBottomSheet(
            onDismissRequest = {
                settingInfo = null
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GameSettingsItemView(
    text: String,
    resource: DrawableResource,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onInfoClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        leadingContent = {
            if (onInfoClick == null) {
                Icon(
                    painter = painterResource(resource),
                    contentDescription = text
                )
            } else {
                Icon(
                    painter = painterResource(resource),
                    contentDescription = "Подробнее: $text",
                    modifier = Modifier.clickable(onClick = onInfoClick)
                )
            }
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

private data class GameSettingInfo(
    val title: String,
    val description: String,
)

private val PLAYER_CARD_HEIGHT = 160.dp
private const val PORTRAIT_PLAYER_COLUMNS = 2
private const val LANDSCAPE_PLAYER_COLUMNS = 4
