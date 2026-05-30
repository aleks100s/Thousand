package com.alextos.thousand.presentation.game.create_game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.presentation.components.GameSettingsView
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.add_24px
import thousand.composeapp.generated.resources.casino_24px
import thousand.composeapp.generated.resources.person_add_24px
import thousand.composeapp.generated.resources.robot_24px

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
                    AnimatedVisibility(state.selectedUsers.count() > 1) {
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
                                settings = state.gameSettings,
                                onSettingsChange = { settings ->
                                    viewModel.onAction(CreateGameAction.UpdateGameSettings(settings))
                                },
                            )
                        }
                        item {
                            Spacer(Modifier.height(100.dp))
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
                    isError = state.newUserNameError != null,
                    supportingText = state.newUserNameError?.let { error ->
                        {
                            Text(error)
                        }
                    },
                    singleLine = true,
                )

                Button(
                    onClick = {
                        viewModel.onAction(CreateGameAction.SaveNewUser)
                    },
                    enabled = state.canSaveNewUser,
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
                Spacer(Modifier.height(100.dp))
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
                Box(
                    modifier = Modifier
                        .border(
                            border = BorderStroke(
                                width = 2.dp,
                                color = if (user.kind == UserKind.MainUser) MaterialTheme.colorScheme.secondary else Color.Transparent,
                            ),
                            shape = CircleShape
                        )
                ) {
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

private val PLAYER_CARD_HEIGHT = 160.dp
private const val PORTRAIT_PLAYER_COLUMNS = 2
private const val LANDSCAPE_PLAYER_COLUMNS = 4
