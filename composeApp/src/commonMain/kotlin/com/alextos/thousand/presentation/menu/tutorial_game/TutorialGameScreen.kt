package com.alextos.thousand.presentation.menu.tutorial_game

import com.alextos.thousand.presentation.components.AppButton
import com.alextos.thousand.presentation.components.AppBottomSheet as ModalBottomSheet
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.components.gameNavigationTitle
import com.alextos.thousand.presentation.components.GameView
import com.alextos.thousand.presentation.menu.game_rules.GameRulesContent
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TutorialGameScreen(
    onGoBack: () -> Unit,
    onFinish: () -> Unit
) {
    val viewModel: TutorialGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isRulesSheetVisible by remember { mutableStateOf(false) }

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

                AppButton(
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
        hideNavigationInLandscape = true,
        modifier = Modifier,
        title = state.gameState.gameNavigationTitle,
        goBack = onGoBack,
        actions = { actionColor ->
            {
                TextButton(
                    onClick = {
                        isRulesSheetVisible = true
                    },
                ) {
                    Text("Правила", color = actionColor)
                }
            }
        },
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
                },
                onPlayerClick = null,
            )
        }
    }

    if (isRulesSheetVisible) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                isRulesSheetVisible = false
            },
        ) {
            GameRulesContent(modifier = Modifier.fillMaxSize())
        }
    }
}
