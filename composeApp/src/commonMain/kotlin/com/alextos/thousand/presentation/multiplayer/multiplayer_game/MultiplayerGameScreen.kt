package com.alextos.thousand.presentation.multiplayer.multiplayer_game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerGameScreen(
    goBack: () -> Unit,
) {
    val viewModel: MultiplayerGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isDeleteGameSheetVisible by remember { mutableStateOf(false) }

    Screen(
        modifier = Modifier,
        title = "Игра ${state.gameCode}",
        goBack = goBack,
        actions = {
            {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    onClick = {
                        isDeleteGameSheetVisible = true
                    },
                ) {
                    Text("Удалить игру")
                }
            }
        },
    ) { modifier ->
        Box(
            modifier = modifier.fillMaxSize(),
        )
    }

    if (isDeleteGameSheetVisible) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                isDeleteGameSheetVisible = false
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Удалить эту игру?",
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = "Это действие нельзя отменить. Игра будет удалена для всех участников.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                    onClick = {
                        isDeleteGameSheetVisible = false
                        viewModel.onAction(MultiplayerGameAction.DeleteGame)
                    },
                ) {
                    Text("Удалить игру")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isDeleteGameSheetVisible = false
                    },
                ) {
                    Text("Отмена")
                }
            }
        }
    }
}
