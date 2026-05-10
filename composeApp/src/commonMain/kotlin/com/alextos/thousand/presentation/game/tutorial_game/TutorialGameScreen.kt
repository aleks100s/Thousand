package com.alextos.thousand.presentation.game.tutorial_game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TutorialGameScreen(
    onGoBack: () -> Unit,
) {
    val viewModel: TutorialGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(TutorialGameAction.Initialize)
    }

    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = onGoBack,
    ) { modifier ->
        Column(
            modifier = modifier.fillMaxSize(),
        ) {
            // Empty for now. This screen will host the tutorial game flow.
        }
    }
}
