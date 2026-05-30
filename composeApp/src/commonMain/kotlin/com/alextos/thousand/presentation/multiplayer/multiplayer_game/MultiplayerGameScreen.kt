package com.alextos.thousand.presentation.multiplayer.multiplayer_game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.components.GameView
import com.alextos.thousand.presentation.game.play_game.components.GameSettingsSheet
import com.alextos.thousand.presentation.game.game_rules.GameRulesContent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerGameScreen(
    goBack: () -> Unit,
) {
    val viewModel: MultiplayerGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val messages = remember { mutableStateListOf<GameMessageBubble>() }
    var nextMessageId by remember { mutableStateOf(0L) }
    var isDeleteGameSheetVisible by remember { mutableStateOf(false) }
    var isRulesSheetVisible by remember { mutableStateOf(false) }
    var isSettingsSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                MultiplayerGameEvent.GameDeleted -> goBack()
                is MultiplayerGameEvent.FinishGame -> goBack()
                is MultiplayerGameEvent.ShowMessage -> {
                    messages.add(
                        GameMessageBubble(
                            id = nextMessageId++,
                            text = event.message,
                            isReply = event.isReply
                        )
                    )
                }
            }
        }
    }

    if (state.error != null) {
        ModalBottomSheet(
            onDismissRequest = goBack
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.error ?: "")

                Button(onClick = goBack) {
                    Text("Выйти в меню")
                }
            }
        }
    }

    Screen(
        modifier = Modifier,
        title = "Игра ${state.gameCode}",
        goBack = goBack,
        actions = {
            {
                TextButton(
                    onClick = {
                        isRulesSheetVisible = true
                    },
                ) {
                    Text("Правила")
                }

                TextButton(
                    onClick = {
                        isSettingsSheetVisible = true
                    },
                ) {
                    Text("Настройки")
                }

                if (state.isHost) {
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
            }
        },
    ) { modifier ->
        GameView(
            modifier = modifier.fillMaxSize(),
            isManualInputEnabled = false,
            state = state.gameState,
            onAction = { action ->
                viewModel.onAction(MultiplayerGameAction.SendGameAction(action))
            },
            onFinishGame = {
                goBack()
            },
        )

        GameMessagesOverlay(
            messages = messages,
            onMessageDismiss = { message ->
                messages.remove(message)
            },
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

    if (isSettingsSheetVisible) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                isSettingsSheetVisible = false
            },
        ) {
            state.gameState.game?.settings?.let { settings ->
                GameSettingsSheet(
                    settings = settings,
                    isNotificationEnabled = settings.isNotificationEnabled,
                    onNotificationEnabledChange = {},
                )
            }
        }
    }
}

@Composable
private fun GameMessagesOverlay(
    messages: List<GameMessageBubble>,
    onMessageDismiss: (GameMessageBubble) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center,
    ) {
        messages.forEach { message ->
            FloatingGameMessage(
                modifier = Modifier.padding(bottom = MESSAGE_BUBBLE_SPACING),
                message = message,
                onFinished = {
                    onMessageDismiss(message)
                },
            )
        }
    }
}

@Composable
private fun FloatingGameMessage(
    modifier: Modifier = Modifier,
    message: GameMessageBubble,
    onFinished: () -> Unit,
) {
    val density = LocalDensity.current
    val offsetY = remember(message.id) { Animatable(0f) }
    val alpha = remember(message.id) { Animatable(1f) }

    LaunchedEffect(message.id) {
        val targetOffset = with(density) { MESSAGE_FLOAT_DISTANCE.toPx() }.unaryMinus()
        coroutineScope {
            launch {
                offsetY.animateTo(
                    targetValue = targetOffset,
                    animationSpec = tween(
                        durationMillis = if (message.isReply) REPLY_ANIMATION_DURATION_MS else MESSAGE_ANIMATION_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
            launch {
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = if (message.isReply) REPLY_ANIMATION_DURATION_MS else MESSAGE_ANIMATION_DURATION_MS,
                        delayMillis = if (message.isReply) REPLY_FADE_DELAY_MS else MESSAGE_FADE_DELAY_MS,
                    ),
                )
            }
        }
        onFinished()
    }

    Text(
        text = message.text,
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY.value
            }
            .alpha(alpha.value)
            .widthIn(max = 280.dp)
            .background(
                color = if (message.isReply) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.inverseSurface,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        color = if (message.isReply) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.inverseOnSurface,
        style = MaterialTheme.typography.bodyMedium,
    )
}

private data class GameMessageBubble(
    val id: Long,
    val text: String,
    val isReply: Boolean = false
)

private val MESSAGE_FLOAT_DISTANCE = 96.dp
private val MESSAGE_BUBBLE_SPACING = 8.dp
private const val MESSAGE_ANIMATION_DURATION_MS = 2500

private const val REPLY_ANIMATION_DURATION_MS = 5000
private const val MESSAGE_FADE_DELAY_MS = 500
private const val REPLY_FADE_DELAY_MS = 3000
