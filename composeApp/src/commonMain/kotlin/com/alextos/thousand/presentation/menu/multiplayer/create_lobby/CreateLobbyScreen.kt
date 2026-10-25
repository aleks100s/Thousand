package com.alextos.thousand.presentation.menu.multiplayer.create_lobby

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.components.GameSettingsView
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLobbyScreen(
    goBack: () -> Unit,
    openLobby: (String) -> Unit,
) {
    val viewModel: CreateLobbyViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateLobbyEvent.OpenLobby -> openLobby(event.lobbyId)
            }
        }
    }

    Screen(
        modifier = Modifier,
        title = "Настройки игры",
        goBack = goBack,
    ) { modifier ->
        GameSettingsView(
            modifier = modifier,
            singlePlayer = false,
            settings = state.gameSettings,
            onSettingsChange = { viewModel.onAction(CreateLobbyAction.UpdateGameSettings(it)) },
            continueText = "Перейти в лобби",
            isContinuing = state.isLoading,
            onContinue = { viewModel.onAction(CreateLobbyAction.OpenLobby) },
        )
    }
}
