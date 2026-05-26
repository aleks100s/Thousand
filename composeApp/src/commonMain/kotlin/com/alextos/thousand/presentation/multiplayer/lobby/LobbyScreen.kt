package com.alextos.thousand.presentation.multiplayer.lobby

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.content_copy_24px
import thousand.composeapp.generated.resources.person_24px
import thousand.composeapp.generated.resources.person_heart_24px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("DEPRECATION")
fun LobbyScreen(
    goBack: () -> Unit,
    openGame: (String) -> Unit,
) {
    val viewModel: LobbyViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LobbyEvent.Disconnect -> goBack()
            }
        }
    }

    if (state.error != null) {
        ModalBottomSheet(
            onDismissRequest = goBack
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(state.error ?: "")

                Button(onClick = goBack) {
                    Text("Понятно")
                }
            }
        }
    }

    Screen(
        modifier = Modifier,
        title = "Лобби ${state.lobbyId}",
        goBack = goBack,
        actions = {
            {
                TextButton(
                    enabled = state.lobbyId.isNotBlank(),
                    onClick = {
                        viewModel.onAction(LobbyAction.LeaveGame)
                    },
                ) {
                    Text("Выйти из игры")
                }
            }
        },
    ) { modifier ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            LoadingView(Modifier.align(Alignment.Center))

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

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Ожидание игроков...",
                )
            }

            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("Подключены:")

                state.players.forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(it.name)

                        Icon(
                            painterResource(Res.drawable.person_24px),
                            contentDescription = null
                        )
                    }
                }
            }

            if (state.isHost) {
                Button(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    enabled = state.isStartButtonEnabled,
                    onClick = {
                        openGame(state.lobbyId)
                    },
                ) {
                    Text("Начать игру")
                }
            }
        }
    }
}

@Composable
private fun LoadingView(modifier: Modifier) {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val scale by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
    )
}