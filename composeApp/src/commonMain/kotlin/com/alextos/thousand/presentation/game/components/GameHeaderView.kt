package com.alextos.thousand.presentation.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.block_24px
import thousand.composeapp.generated.resources.person_24px
import thousand.composeapp.generated.resources.person_heart_24px
import thousand.composeapp.generated.resources.robot_24px
import thousand.composeapp.generated.resources.trophy_24px

@Composable
fun GameHeaderView(
    game: Game,
    currentPlayer: Player? = null,
    showBolts: Boolean = false
) {
    val shouldScroll = game.players.size > 2

    val content: @Composable (Player) -> Unit = { player ->
        PlayerView(player, currentPlayer == player, showBolts && game.settings.isTripleBoltFineActive)
    }

    if (shouldScroll) {
        val scrollState = rememberLazyListState()

        LaunchedEffect(currentPlayer) {
            val index = game.players.indexOfFirst { it == currentPlayer }
            if (index >= 0) {
                scrollState.animateScrollToItem(index)
            }
        }

        LazyRow(
            state = scrollState,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Spacer(Modifier.width(16.dp))
            }

            items(game.players) { player ->
                content(player)
            }

            item {
                Spacer(Modifier.width(16.dp))
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            game.players.forEach {
                content(it)
            }
        }
    }
}

@Composable
private fun PlayerView(player: Player, isActive: Boolean, showBolts: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1f,
        animationSpec = tween(durationMillis = 1000)
    )
    var previousScore by remember(player) { mutableStateOf<Int?>(null) }
    var nextBubbleId by remember(player) { mutableStateOf(0L) }
    var scoreBubble by remember(player) { mutableStateOf<ScoreChangeBubble?>(null) }

    LaunchedEffect(player.currentScore) {
        val previous = previousScore
        if (previous != null) {
            val scoreChange = player.currentScore - previous
            if (scoreChange != 0) {
                scoreBubble = ScoreChangeBubble(
                    id = nextBubbleId++,
                    value = scoreChange
                )
            }
        }
        previousScore = player.currentScore
    }

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(
                        if (player.isWinner)
                            Res.drawable.trophy_24px
                        else if (player.isBot())
                            Res.drawable.robot_24px
                        else if (player.isMain())
                            Res.drawable.person_heart_24px
                        else
                            Res.drawable.person_24px
                    ),
                    contentDescription = null,
                    tint = if (player.isWinner) Color.Yellow else if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = player.header(),
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )

                if (showBolts) {
                    (1..player.boltCount).forEach { _ ->
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(Res.drawable.block_24px),
                            tint = MaterialTheme.colorScheme.secondary,
                            contentDescription = "Пропущенный ход"
                        )
                    }
                }
            }

            Text(
                text = "${player.currentScore} очков",
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
        }

        scoreBubble?.let { bubble ->
            ScoreChangeBubbleView(
                modifier = Modifier.align(Alignment.BottomCenter),
                bubble = bubble,
                onFinished = {
                    if (scoreBubble?.id == bubble.id) {
                        scoreBubble = null
                    }
                }
            )
        }
    }
}

@Composable
private fun ScoreChangeBubbleView(
    modifier: Modifier = Modifier,
    bubble: ScoreChangeBubble,
    onFinished: () -> Unit,
) {
    val density = LocalDensity.current
    val offsetY = remember(bubble.id) { Animatable(0f) }
    val alpha = remember(bubble.id) { Animatable(1f) }
    val isPositive = bubble.value > 0

    LaunchedEffect(bubble.id) {
        val targetOffset = with(density) { SCORE_CHANGE_FLOAT_DISTANCE.toPx() }.unaryMinus()
        coroutineScope {
            launch {
                offsetY.animateTo(
                    targetValue = targetOffset,
                    animationSpec = tween(
                        durationMillis = SCORE_CHANGE_ANIMATION_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
            launch {
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = SCORE_CHANGE_ANIMATION_DURATION_MS,
                        delayMillis = SCORE_CHANGE_FADE_DELAY_MS,
                    ),
                )
            }
        }
        onFinished()
    }

    Text(
        text = if (isPositive) "+${bubble.value}" else bubble.value.toString(),
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY.value
            }
            .alpha(alpha.value)
            .background(
                color = if (isPositive) POSITIVE_SCORE_CHANGE_COLOR else NEGATIVE_SCORE_CHANGE_COLOR,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = Color.White,
        style = MaterialTheme.typography.labelMedium,
    )
}

private data class ScoreChangeBubble(
    val id: Long,
    val value: Int,
)

private val SCORE_CHANGE_FLOAT_DISTANCE = 56.dp
private const val SCORE_CHANGE_ANIMATION_DURATION_MS = 1800
private const val SCORE_CHANGE_FADE_DELAY_MS = 300
private val POSITIVE_SCORE_CHANGE_COLOR = Color(0xFF2E7D32)
private val NEGATIVE_SCORE_CHANGE_COLOR = Color(0xFFC62828)
