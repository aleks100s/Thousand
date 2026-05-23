package com.alextos.thousand.presentation.multiplayer.lobby

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.content_copy_24px

@Composable
@Suppress("DEPRECATION")
fun LobbyScreen(
    goBack: () -> Unit,
) {
    val viewModel: LobbyViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    Screen(
        modifier = Modifier,
        title = "Лобби ${state.lobbyId}",
        goBack = goBack,
        actions = {
            {
                TextButton(
                    enabled = state.lobbyId.isNotBlank(),
                    onClick = {
                        clipboardManager.setText(AnnotatedString(state.lobbyId))
                    },
                ) {
                    Text("ID лобби")

                    Icon(
                        painter = painterResource(Res.drawable.content_copy_24px),
                        contentDescription = "Скопировать ID лобби",
                    )
                }
            }
        },
    ) { modifier ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                state.gameSettings.players.forEach {
                    Text(it.name)
                }

                CircularProgressIndicator()

                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Ожидание игроков",
                )
            }

            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                enabled = state.isStartButtonEnabled,
                onClick = {},
            ) {
                Text("Начать игру")
            }
        }
    }
}
