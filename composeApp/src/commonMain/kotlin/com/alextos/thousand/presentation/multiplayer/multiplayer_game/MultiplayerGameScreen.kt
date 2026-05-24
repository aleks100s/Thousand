package com.alextos.thousand.presentation.multiplayer.multiplayer_game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MultiplayerGameScreen(
    goBack: () -> Unit,
) {
    val viewModel: MultiplayerGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Screen(
        modifier = Modifier,
        title = "Игра ${state.lobbyId}",
        goBack = goBack,
    ) { modifier ->
        Box(
            modifier = modifier.fillMaxSize(),
        )
    }
}
