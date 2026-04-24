package com.alextos.thousand.presentation.game.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.bolt_24px
import thousand.composeapp.generated.resources.person_24px
import thousand.composeapp.generated.resources.trophy_24px

@Composable
fun GameHeaderView(
    game: Game,
    currentPlayer: Player? = null,
    showBolts: Boolean = false
) {
    val shouldScroll = game.players.size > 2

    val content: @Composable (Player) -> Unit = { player ->
        PlayerView(player, currentPlayer == player, showBolts && game.isTripleBoltFineActive)

        if (game.players.count() > 1 && game.players.lastOrNull() != player) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                text = "vs",
                color = MaterialTheme.colorScheme.error
            )
        }
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(if (player.isWinner) Res.drawable.trophy_24px else Res.drawable.person_24px),
                contentDescription = null,
                tint = if (player.isWinner) Color.Yellow else if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = player.toString(),
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )

            if (showBolts) {
                (1..player.boltCount).forEach { i ->
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(Res.drawable.bolt_24px),
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
}