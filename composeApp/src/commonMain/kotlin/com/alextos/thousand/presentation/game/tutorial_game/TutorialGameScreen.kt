package com.alextos.thousand.presentation.game.tutorial_game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.game.components.GameView
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TutorialGameScreen(
    onGoBack: () -> Unit,
    onFinish: () -> Unit
) {
    val viewModel: TutorialGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(TutorialGameAction.Initialize)
    }

    state.messageToShow?.let { message ->
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.onAction(TutorialGameAction.CloseMessage)
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Text(text = message)

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.onAction(TutorialGameAction.CloseMessage)
                    }
                ) {
                    Text("Понятно")
                }
            }
        }
    }

    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = onGoBack,
    ) { modifier ->
        Column(
            modifier = modifier.fillMaxSize(),
        ) {
            GameView(
                modifier = Modifier.fillMaxSize(),
                isManualInputEnabled = false,
                state = state.gameState,
                onAction = { action ->
                    viewModel.onAction(TutorialGameAction.SendGameAction(action))
                },
                onFinishGame = {
                    onFinish()
                }
            )
        }
    }
}
