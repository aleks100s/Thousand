package com.alextos.thousand.presentation.game.play_game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.presentation.game.components.GameHeaderView
import com.alextos.thousand.presentation.game.components.GameRulesView
import com.alextos.thousand.presentation.game.components.RollingDiceView
import com.alextos.thousand.presentation.game.components.SingleDieView
import com.alextos.thousand.presentation.game.play_game.components.ManualDiceInputView
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.back_hand_24px
import thousand.composeapp.generated.resources.casino_24px

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayGameScreen(
    onGoBack: () -> Unit,
    onScoreClick: (Game) -> Unit,
    onFinishGame: (Game) -> Unit
) {
    val viewModel: PlayGameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var isRulesSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onAction(PlayGameAction.LoadGame)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PlayGameEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message, withDismissAction = true)
                }
            }
        }
    }
    
    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = onGoBack,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        actions = {
            {
                TextButton(
                    onClick = {
                        isRulesSheetVisible = true
                    }
                ) {
                    Text("Правила")
                }

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
            var buttonsVisible by remember { mutableStateOf(true) }

            LaunchedEffect(state.currentRoll) {
                val delay = (state.currentRoll?.dice?.count() ?: 0) * 200L
                if (delay > 0L) {
                    buttonsVisible = false
                    delay(delay)
                    buttonsVisible = true
                } else {
                    buttonsVisible = true
                }
                viewModel.onAction(PlayGameAction.FinishRoll)
            }

            AnimatedVisibility(visible = buttonsVisible) {
                ButtonsView(state, onFinishGame, viewModel)
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
                GameHeaderView(game, state.currentPlayer, showBolts = true)

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    state.currentRoll?.let { roll ->
                        CurrentRollView(roll, animate = state.isManualInputEnabled.not())
                    } ?: run {
                        if (state.isEmpty()) {
                            Text("Игра началась!")
                        } else {
                            Text("Продолжение игры")
                        }
                    }

                    if (state.currentTurn.count() > 1) {
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

    if (isRulesSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                isRulesSheetVisible = false
            }
        ) {
            GameRulesView()
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
private fun ButtonsView(
    state: PlayGameState,
    onFinishGame: (Game) -> Unit,
    viewModel: PlayGameViewModel
) {
    var isManualInputShown by remember { mutableStateOf(false) }

    if (isManualInputShown) {
        ModalBottomSheet(
            onDismissRequest = {
                isManualInputShown = false
            }
        ) {
            ManualDiceInputView(count = state.rollAbility.count) {
                isManualInputShown = false
                viewModel.onAction(PlayGameAction.ApplyDiceRoll(it))
            }
        }
    }

    if (state.game?.isFinished() == true) {
        AnimatedVisibility(state.rollAbility != RollAbility.REQUIRED) {
            ExtendedFloatingActionButton(
                onClick = {
                    onFinishGame(state.game)
                },
                text = {
                    Text("Закончить игру")
                },
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.back_hand_24px),
                        contentDescription = "Закончить игру"
                    )
                },
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                shape = FloatingActionButtonDefaults.smallExtendedFabShape
            )
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AnimatedVisibility(state.rollAbility != RollAbility.REQUIRED) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.onAction(PlayGameAction.FinishTurn)
                    },
                    text = {
                        Text("Закончить")
                    },
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.back_hand_24px),
                            contentDescription = "Закончить"
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
                        if (state.isManualInputEnabled) {
                            isManualInputShown = true
                        } else {
                            viewModel.onAction(PlayGameAction.RollTheDice)
                        }
                    },
                    text = {
                        val text = when (state.rollAbility) {
                            RollAbility.UNAVAILABLE -> ""
                            RollAbility.REQUIRED -> "Бросить кубики"
                            else -> "Еще раз (${state.rollAbility.count})"
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
}

@Composable
private fun CurrentRollView(roll: DiceRoll, animate: Boolean = true) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            roll.dice.forEachIndexed { index, die ->
                RollingDiceView(die, delay = if (animate) (index + 1) * 250L else 0L)
            }
        }

        var isResultVisible by remember(roll) { mutableStateOf<Boolean?>(null) }
        LaunchedEffect(roll) {
            if (animate) {
                isResultVisible = false
                delay(250L * roll.dice.count())
            }
            isResultVisible = true
        }

        isResultVisible?.let {
            AnimatedVisibility(it) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalDivider(Modifier.width(220.dp))

                    Text(roll.result.toString())
                }
            }
        }
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

        HorizontalDivider(Modifier.width(80.dp))

        Text(rolls.sumOf { it.result }.toString())
    }
}

@Composable
private fun RollHistoryView(roll: DiceRoll) {
    Row {
        roll.dice.forEach { die ->
            SingleDieView(Modifier.size(16.dp), die.value)
        }
    }
}
