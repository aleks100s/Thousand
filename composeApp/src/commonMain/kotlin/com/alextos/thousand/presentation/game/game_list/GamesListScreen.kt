package com.alextos.thousand.presentation.game.game_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.Game
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GamesListScreen(
    onGameClick: (Game) -> Unit,
) {
    val viewModel: GamesListViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(GamesListAction.LoadGames)
    }

    Screen(
        modifier = Modifier,
        title = "Список игр",
    ) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize()) {
                LoadingIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = state.games,
                    key = { game -> game.id },
                ) { game ->
                    GameItem(game) {
                        onGameClick(game)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameItem(game: Game, onGameClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onGameClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = "Игра №${game.id}")

                Text(
                    text = game.players.joinToString(separator = " vs "),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            GameStatus(game = game)
        }
    }
}

@Composable
private fun RowScope.GameStatus(game: Game) {
    if (game.isFinished()) {
        val winner = game.players.firstOrNull { it.isWinner }?.user?.name ?: "-"
        val finishedAt = game.finishedAt?.formatForUi().orEmpty()

        Column {
            Text(
                text = "Победитель: $winner",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = finishedAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Box(
        modifier = Modifier
            .size(14.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
            .clip(CircleShape)
            .background(Color.Red),
    )
}

private fun kotlin.time.Instant.formatForUi(): String {
    val dateTime = toLocalDateTime(TimeZone.currentSystemDefault())

    return buildString {
        append(dateTime.day.toString().padStart(2, '0'))
        append('.')
        append((dateTime.month.ordinal + 1).toString().padStart(2, '0'))
        append('.')
        append(dateTime.year)
        append(' ')
        append(dateTime.hour.toString().padStart(2, '0'))
        append(':')
        append(dateTime.minute.toString().padStart(2, '0'))
    }
}
