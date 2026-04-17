package com.alextos.thousand.presentation.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import thousand.composeapp.generated.resources.casino_24px
import thousand.composeapp.generated.resources.person_24px
import thousand.composeapp.generated.resources.trophy_24px

@Composable
fun GameHeaderView(game: Game, currentPlayer: Player? = null) {
    Row(
        modifier = Modifier
            // .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        game.players.forEach { player ->
            PlayerView(player, currentPlayer == player)

            if (game.players.firstOrNull() == player) {
                Text(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    text = "vs",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun PlayerView(player: Player, isActive: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 1000)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Row {
            Icon(
                painter = painterResource(if (player.isWinner) Res.drawable.trophy_24px else Res.drawable.person_24px),
                contentDescription = null,
                tint = if (player.isWinner) Color.Yellow else if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = player.toString(),
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )

            AnimatedVisibility(isActive) {
                Icon(
                    painter = painterResource(Res.drawable.casino_24px),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Активный игрок"
                )
            }
        }

        Text(
            text = "${player.currentScore} очков",
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
        )
    }
}