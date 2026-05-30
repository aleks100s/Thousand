package com.alextos.thousand.presentation.game.game_list

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.InfoCardView
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.game.components.GameStatusView
import com.alextos.thousand.presentation.models.GameUi
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.add_24px
import thousand.composeapp.generated.resources.casino_24px
import thousand.composeapp.generated.resources.notifications_24px
import thousand.composeapp.generated.resources.notifications_off_24px
import thousand.composeapp.generated.resources.sports_esports_24px

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GamesListScreen(
    onGameClick: (GameUi) -> Unit,
    openGame: (Long) -> Unit,
    onCreateGame: () -> Unit,
    onTutorialGame: () -> Unit,
) {
    val viewModel: GamesListViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isTutorialSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onAction(GamesListAction.LoadGames)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is GamesListEvent.OpenGame -> openGame(event.gameId)
            }
        }
    }

    Screen(
        modifier = Modifier,
        title = "Список игр",
        floatingActionButton = {
            if (state.isFABShown) {
                ExtendedFloatingActionButton(
                    text = {
                        Text("Начать игру")
                    },
                    icon = {
                        Icon(
                            painterResource(Res.drawable.add_24px),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        if (state.isFirstLaunch) {
                            isTutorialSheetVisible = true
                        } else {
                            onCreateGame()
                        }
                    },
                )
            }
        },
    ) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), Alignment.Center) {
                LoadingIndicator()
            }
        } else if (state.games.isEmpty()) {
            Column (
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                InfoCardView(
                    icon = Res.drawable.casino_24px,
                    title = "Здесь будут ваши игры",
                    text = "Создайте новую партию, чтобы собрать игроков, бросать кубики и сохранить историю счета.",
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (state.isFirstLaunch) {
                                isTutorialSheetVisible = true
                            } else {
                                onCreateGame()
                            }
                        },
                    ) {
                        Text("Начать игру")
                    }
                }
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
                        onRematchGame = {
                            viewModel.onAction(GamesListAction.CreateRematch(game.id))
                        },
                    )
                }

                item {
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }

    if (isTutorialSheetVisible) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                isTutorialSheetVisible = false
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Хотите пройти обучение и сыграть тестовую игру?",
                    style = MaterialTheme.typography.titleMedium,
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isTutorialSheetVisible = false
                        viewModel.onAction(GamesListAction.CompleteFirstLaunch)
                        onTutorialGame()
                    },
                ) {
                    Text("Да, хочу научиться")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isTutorialSheetVisible = false
                        viewModel.onAction(GamesListAction.CompleteFirstLaunch)
                        onCreateGame()
                    },
                ) {
                    Text("Нет, я умею играть")
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
    onRematchGame: () -> Unit,
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
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = game.title)

                        Icon(
                            painter = painterResource(if (game.isNotificationEnabled) Res.drawable.notifications_24px else Res.drawable.notifications_off_24px),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )

                        Icon(
                            painter = painterResource(if (game.isVirtualDiceEnabled) Res.drawable.sports_esports_24px else Res.drawable.casino_24px),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    GameStatusView(game.isFinished, game.finishedAt)
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
            if (game.isFinished) {
                DropdownMenuItem(
                    text = {
                        Text("Реванш")
                    },
                    onClick = {
                        isMenuExpanded = false
                        onRematchGame()
                    },
                )
            }

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