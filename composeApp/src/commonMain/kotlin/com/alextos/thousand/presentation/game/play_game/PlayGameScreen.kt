package com.alextos.thousand.presentation.game.play_game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlayGameScreen(
    onGoBack: () -> Unit,
) {
    val viewModel: PlayGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(PlayGameAction.LoadGame)
    }

    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = onGoBack,
    ) { modifier ->
        Box(modifier = modifier.fillMaxSize())
    }
}
