package com.alextos.thousand.presentation.menu.create_game

import com.alextos.thousand.application.theme.AppColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.components.GameSettingsView
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.close_24px
import thousand.composeapp.generated.resources.edit_24px

@Composable
fun CreateGameScreen(openGame: (Long) -> Unit, goBack: () -> Unit) {
    val viewModel: CreateGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.onAction(CreateGameAction.Initialize) }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) { is CreateGameEvent.OpenGame -> openGame(event.gameId) }
        }
    }
    CreateGameContent(state, viewModel::onAction, goBack)
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun CreateGameContent(state: CreateGameState, onAction: (CreateGameAction) -> Unit, goBack: () -> Unit) {
    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = {
            when {
                state.step == CreateGameStep.Settings -> onAction(CreateGameAction.OpenPlayersStep)
                state.isEditingPlayers -> onAction(CreateGameAction.FinishPlayersEditing)
                else -> goBack()
            }
        },
        actions = { actionColor ->
            {
                if (state.step == CreateGameStep.Players) {
                    IconButton(enabled = !state.isPlayerOperationRunning && !state.isLoadingUsers,
                        onClick = { onAction(CreateGameAction.TogglePlayersEditing) }) {
                        Icon(painterResource(if (state.isEditingPlayers) Res.drawable.close_24px else Res.drawable.edit_24px),
                            contentDescription = if (state.isEditingPlayers) "Завершить редактирование" else "Редактировать игроков",
                            tint = actionColor)
                    }
                }
            }
        },
    ) { modifier ->
        if (state.isLoadingUsers) {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingIndicator() }
        } else {
            when (state.step) {
                CreateGameStep.Players -> PlayersSelectionView(
                    modifier = modifier,
                    users = state.users,
                    selectedUsers = state.selectedUsers,
                    isEditing = state.isEditingPlayers,
                    isBusy = state.isPlayerOperationRunning,
                    error = state.playerError.takeIf { !state.isPlayerEditorVisible },
                    onAddUser = { onAction(CreateGameAction.ShowAddUserSheet) },
                    onAddBot = { onAction(CreateGameAction.ShowAddBotSheet) },
                    onToggleUser = { onAction(CreateGameAction.ToggleUserSelection(it)) },
                    onEdit = { onAction(CreateGameAction.EditPlayer(it)) },
                    onDelete = { onAction(CreateGameAction.RequestDeletePlayer(it)) },
                    onContinue = { onAction(if (state.isEditingPlayers) CreateGameAction.FinishPlayersEditing else CreateGameAction.OpenSettingsStep) },
                )
                CreateGameStep.Settings -> GameSettingsView(
                    modifier = modifier,
                    settings = state.gameSettings,
                    onSettingsChange = { onAction(CreateGameAction.UpdateGameSettings(it)) },
                    continueText = "Начать игру",
                    onContinue = { onAction(CreateGameAction.CreateGame) },
                )
            }
        }
    }
    if (state.isPlayerEditorVisible) PlayerEditorSheet(state, onAction)
    state.pendingDeleteUser?.let { user ->
        AlertDialog(
            onDismissRequest = { onAction(CreateGameAction.CancelDeletePlayer) },
            containerColor = AppColors.Sheet, titleContentColor = AppColors.TextPrimary, textContentColor = AppColors.TextSecondary,
            title = { Text("Удалить игрока?") },
            text = { Text("Игрок «${user.name}» будет удалён из списка локальных игроков.") },
            confirmButton = { TextButton(onClick = { onAction(CreateGameAction.ConfirmDeletePlayer) }) { Text("Удалить", color = AppColors.Danger) } },
            dismissButton = { TextButton(onClick = { onAction(CreateGameAction.CancelDeletePlayer) }) { Text("Отмена", color = AppColors.TextSecondary) } },
        )
    }
}
