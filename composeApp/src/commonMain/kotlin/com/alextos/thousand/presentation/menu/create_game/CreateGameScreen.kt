package com.alextos.thousand.presentation.menu.create_game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.presentation.components.GameSettingsView
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.settings_24px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGameScreen(
    openGame: (Long) -> Unit,
    goBack: () -> Unit,
    openUsers: () -> Unit,
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
                CreateGameStep.Settings -> viewModel.onAction(
                    CreateGameAction.OpenPlayersStep)
            }
        },
        actions = {
            {
                if (state.step == CreateGameStep.Players) {
                    IconButton(
                        onClick = openUsers,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.settings_24px),
                            contentDescription = "Настройки игроков"
                        )
                    }
                }
            }
        },
    ) { modifier ->
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (state.step) {
                CreateGameStep.Players -> {
                    PlayersSelectionView(
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
                            viewModel.onAction(
                                CreateGameAction.ToggleUserSelection(
                                    user
                                )
                            )
                        },
                        onContinue = { viewModel.onAction(CreateGameAction.OpenSettingsStep) },
                    )
                }
                CreateGameStep.Settings -> {
                    GameSettingsView(
                        settings = state.gameSettings,
                        onSettingsChange = { viewModel.onAction(CreateGameAction.UpdateGameSettings(it)) },
                        continueText = "Начать игру",
                        onContinue = { viewModel.onAction(CreateGameAction.CreateGame) },
                    )
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
