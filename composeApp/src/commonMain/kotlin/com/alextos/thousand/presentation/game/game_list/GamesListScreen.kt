package com.alextos.thousand.presentation.game.game_list

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.models.GameUi
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.add_24px
import thousand.composeapp.generated.resources.casino_24px
import thousand.composeapp.generated.resources.notifications_24px
import thousand.composeapp.generated.resources.notifications_off_24px
import thousand.composeapp.generated.resources.sports_esports_24px
import thousand.composeapp.generated.resources.trophy_24px

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GamesListScreen(
    onGameClick: (GameUi) -> Unit,
    onCreateGame: () -> Unit
) {
    val viewModel: GamesListViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(GamesListAction.LoadGames)
    }

    Screen(
        modifier = Modifier,
        title = "Список игр",
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text("Новая игра")
                },
                icon = {
                    Icon(
                        painterResource(Res.drawable.add_24px),
                        contentDescription = null
                    )
                },
                onClick = onCreateGame
            )
        }
    ) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), Alignment.Center) {
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
                    GameItem(
                        game = game,
                        onGameClick = { onGameClick(game) },
                        onDeleteGame = {
                            viewModel.onAction(GamesListAction.DeleteGame(game.id))
                        },
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameItem(
    game: GameUi,
    onGameClick: () -> Unit,
    onDeleteGame: () -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onGameClick,
                    onLongClick = {
                        isMenuExpanded = true
                    },
                ),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = game.title)

                    GameStatus(game)
                }

                Text(
                    text = game.opponents,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = {
                isMenuExpanded = false
            },
        ) {
            DropdownMenuItem(
                text = {
                    Text("Удалить игру")
                },
                onClick = {
                    isMenuExpanded = false
                    onDeleteGame()
                },
                colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error)
            )
        }
    }
}

@Composable
private fun GameStatus(game: GameUi) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(if (game.isNotificationEnabled) Res.drawable.notifications_24px else Res.drawable.notifications_off_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )

        Icon(
            painter = painterResource(if (game.isVirtualDiceEnabled) Res.drawable.sports_esports_24px else Res.drawable.casino_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )

        if (game.isFinished && game.winnerName != null) {
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = game.winnerName)

                    Icon(
                        painter = painterResource(Res.drawable.trophy_24px),
                        contentDescription = "Победитель",
                        tint = Color.Yellow
                    )
                }

                Text(
                    text = game.finishedAt.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
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
}
