package com.alextos.thousand.presentation.game.play_game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.presentation.game.components.GameHeaderView
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.back_hand_24px
import thousand.composeapp.generated.resources.casino_24px

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayGameScreen(
    onGoBack: () -> Unit,
    onScoreClick: (Game) -> Unit
) {
    val viewModel: PlayGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(PlayGameAction.LoadGame)
    }

    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = onGoBack,
        actions = {
            {
                state.game?.let {
                    TextButton(onClick = {
                        onScoreClick(it)
                    }) {
                        Text("Счет")
                    }
                }
            }
        },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AnimatedVisibility(state.rollAbility != RollAbility.REQUIRED) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            viewModel.onAction(PlayGameAction.FinishTurn)
                        },
                        text = {
                            Text("Закончить ход")
                        },
                        icon = {
                            Icon(
                                painter = painterResource(Res.drawable.back_hand_24px),
                                contentDescription = "Закончить ход"
                            )
                        },
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        shape = FloatingActionButtonDefaults.smallExtendedFabShape
                    )
                }

                AnimatedVisibility(state.rollAbility != RollAbility.UNAVAILABLE) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            viewModel.onAction(PlayGameAction.RollTheDice)
                        },
                        text = {
                            val text = when (state.rollAbility) {
                                RollAbility.UNAVAILABLE -> ""
                                RollAbility.REQUIRED -> "Бросить кубики"
                                else -> "Перебросить (${state.rollAbility.count})"
                            }
                            Text(text)
                        },
                        icon = {
                            Icon(
                                painter = painterResource(Res.drawable.casino_24px),
                                contentDescription = "Бросить кубики"
                            )
                        },
                        shape = FloatingActionButtonDefaults.smallExtendedFabShape
                    )
                }
            }
        }
    ) { modifier ->
        val game = state.game
        if (game == null) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            Column(modifier = modifier.fillMaxSize()) {
                GameHeaderView(game, currentPlayer = state.currentPlayer)

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (state.isEmpty()) {
                        Text("Игра началась!")
                    }

                    state.currentRoll?.let { roll ->
                        CurrentRollView(roll)
                    }

                    TurnHistoryView(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart),
                        turn = state.currentTurn
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentRollView(roll: DiceRoll) {
    Column {
        Row {
            roll.dice.forEach { die ->
                Text(text = die.value.value.toString())
            }
        }

        Text(roll.result.toString())
    }
}

@Composable
private fun TurnHistoryView(
    modifier: Modifier,
    turn: List<DiceRoll>
) {
    val rolls = remember(turn) {
        turn.dropLast(1)
    }

    Column(modifier) {
        rolls.forEach { roll ->
            RollHistoryView(roll)
        }

        HorizontalDivider()

        Text(rolls.sumOf { it.result }.toString())
    }
}

@Composable
private fun RollHistoryView(roll: DiceRoll) {
    Row {
        roll.dice.forEach { die ->
            Text(die.toString())
        }
    }
}