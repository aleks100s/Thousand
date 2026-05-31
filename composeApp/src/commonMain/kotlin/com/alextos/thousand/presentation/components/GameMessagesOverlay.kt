package com.alextos.thousand.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class GameMessageBubble(
    val id: Long,
    val text: String,
    val isReply: Boolean = false,
)

@Composable
fun GameMessagesOverlay(
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
                        durationMillis = if (message.isReply) {
                            REPLY_ANIMATION_DURATION_MS
                        } else {
                            MESSAGE_ANIMATION_DURATION_MS
                        },
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
            launch {
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = if (message.isReply) {
                            REPLY_ANIMATION_DURATION_MS
                        } else {
                            MESSAGE_ANIMATION_DURATION_MS
                        },
                        delayMillis = if (message.isReply) {
                            REPLY_FADE_DELAY_MS
                        } else {
                            MESSAGE_FADE_DELAY_MS
                        },
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
                color = if (message.isReply) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.inverseSurface
                },
                shape = RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        color = if (message.isReply) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.inverseOnSurface
        },
        style = MaterialTheme.typography.bodyMedium,
    )
}

private val MESSAGE_FLOAT_DISTANCE = 96.dp
private val MESSAGE_BUBBLE_SPACING = 8.dp
private const val MESSAGE_ANIMATION_DURATION_MS = 2500
private const val REPLY_ANIMATION_DURATION_MS = 5000
private const val MESSAGE_FADE_DELAY_MS = 500
private const val REPLY_FADE_DELAY_MS = 3000
