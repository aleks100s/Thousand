package com.alextos.thousand.presentation.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.game.TutorialNextAction
import com.alextos.thousand.domain.game.server.GameAction
import com.alextos.thousand.domain.game.server.GameState
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.presentation.game.play_game.components.ManualDiceInputView
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GameView(
    modifier: Modifier,
    isManualInputEnabled: Boolean,
    state: GameState,
    onAction: (GameAction) -> Unit,
    onFinishGame: () -> Unit
) {
    val isAnimated = isManualInputEnabled.not() || state.currentPlayer?.isBot() == true
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
                    CurrentRollView(roll, animate = isAnimated)
                } ?: run {
                    Text("Вы в игре!")
                }

                if (state.currentTurn.count() > 1) {
                    TurnHistoryView(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart),
                        turn = state.currentTurn
                    )
                }

                var buttonsVisible by remember { mutableStateOf(true) }

                LaunchedEffect(state.currentRoll) {
                    val delay = (state.currentRoll?.dice?.count() ?: 0) * 250L
                    if (delay > 0L && isAnimated) {
                        buttonsVisible = false
                        delay(delay)
                        buttonsVisible = true
                    } else {
                        buttonsVisible = true
                    }

                    onAction(GameAction.FinishRoll)
                }

                if (buttonsVisible) {
                    ButtonsView(
                        Modifier.align(Alignment.BottomCenter),
                        state,
                        isManualInputEnabled,
                        onAction = {
                            buttonsVisible = false
                            onAction(it)
                        },
                        onFinishGame = onFinishGame
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentRollView(
    roll: DiceRoll,
    animate: Boolean = true
) {
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

                    if (roll.rollDescription != null) {
                        Text(
                            text = roll.rollDescription,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(roll.result.toString())
                    }
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

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
private fun ButtonsView(
    modifier: Modifier,
    state: GameState,
    isManualInputEnabled: Boolean,
    onAction: (GameAction) -> Unit,
    onFinishGame: () -> Unit
) {
    var isManualInputShown by remember { mutableStateOf(false) }
    val shouldUseManualInput = isManualInputEnabled && state.isTutorial.not()
    val tutorialNextAction = state.tutorialNextAction
    val hasTutorialAction = state.isTutorial && tutorialNextAction != null
    val isFinishTurnVisible = if (state.isTutorial) {
        state.rollAbility != RollAbility.REQUIRED
    } else {
        state.rollAbility != RollAbility.REQUIRED && state.isFinishTurnBlocked.not()
    }
    val isRollVisible = state.rollAbility != RollAbility.UNAVAILABLE
    val isFinishTurnEnabled = if (hasTutorialAction) {
        tutorialNextAction == TutorialNextAction.FinishTurn && state.isFinishTurnBlocked.not()
    } else {
        state.isFinishTurnBlocked.not()
    }
    val isRollEnabled = if (hasTutorialAction) {
        tutorialNextAction == TutorialNextAction.Reroll
    } else {
        true
    }
    val tutorialAdvice = when {
        state.isTutorial.not() -> null
        isFinishTurnVisible && isFinishTurnEnabled.not() -> state.tutorialAdvice
        isRollVisible && isRollEnabled.not() -> state.tutorialAdvice
        isFinishTurnVisible && isRollVisible.not() && tutorialNextAction == TutorialNextAction.FinishTurn -> state.tutorialAdvice
        isRollVisible && isFinishTurnVisible.not() && tutorialNextAction == TutorialNextAction.Reroll -> state.tutorialAdvice
        else -> null
    }

    if (isManualInputShown) {
        ModalBottomSheet(
            onDismissRequest = {
                isManualInputShown = false
            }
        ) {
            ManualDiceInputView(count = state.rollAbility.count) {
                isManualInputShown = false
                onAction(GameAction.ApplyRoll(it))
            }
        }
    }

    Column(
        modifier = modifier.padding(16.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.game?.isFinished() == true) {
                if (state.rollAbility != RollAbility.REQUIRED) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onFinishGame()
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        )
                    ) {
                        Text("Закончить игру")
                    }
                }
            } else if (state.currentPlayer?.isBot() == false) {
                if (isFinishTurnVisible) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = isFinishTurnEnabled,
                        onClick = {
                            onAction(GameAction.FinishTurn)
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text("Закончить ход")
                    }
                }

                if (isRollVisible) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = isRollEnabled,
                        onClick = {
                            if (shouldUseManualInput) {
                                isManualInputShown = true
                            } else {
                                onAction(GameAction.RollDice)
                            }
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        val text = when (state.rollAbility) {
                            RollAbility.UNAVAILABLE -> ""
                            RollAbility.REQUIRED -> "Бросить кубики"
                            else -> "Перебросить (${state.rollAbility.count})"
                        }
                        Text(text)
                    }
                }
            }
        }

        AnimatedVisibility(tutorialAdvice != null) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = tutorialAdvice ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}
