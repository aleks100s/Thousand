package com.alextos.thousand.presentation.multiplayer.create_lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.onAction(CreateLobbyAction.OpenLobby)
                },
            ) {
                Text("Перейти в лобби")
            }
        },
    ) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                GameSettingsView(
                    singlePlayer = false,
                    settings = state.gameSettings,
                    onSettingsChange = { settings ->
                        viewModel.onAction(CreateLobbyAction.UpdateGameSettings(settings))
                    },
                )
            }
            item {
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}
