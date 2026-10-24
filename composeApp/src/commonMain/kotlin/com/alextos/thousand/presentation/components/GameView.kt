package com.alextos.thousand.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.usecase.game.TutorialNextAction
import com.alextos.thousand.domain.usecase.game.server.GameAction
import com.alextos.thousand.domain.usecase.game.server.GameState
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.GameButton
import com.alextos.thousand.presentation.menu.play_game.components.ManualDiceInputView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.alextos.thousand.presentation.components.dice.DiceSceneBounds
import com.alextos.thousand.presentation.components.dice.DiceSceneConfiguration
import com.alextos.thousand.presentation.components.dice.DiceSceneCoordinator
import com.alextos.thousand.presentation.components.dice.DiceScenePhase
import com.alextos.thousand.presentation.components.dice.DiceSceneSurface

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GameView(
    modifier: Modifier,
    isManualInputEnabled: Boolean,
    state: GameState,
    onAction: (GameAction) -> Unit,
    onFinishGame: () -> Unit,
    onPlayerClick: ((Player) -> Unit)? = null,
) {
    val isAnimated = isManualInputEnabled.not() || state.currentPlayer?.isBot() == true
    val game = state.game
    if (game == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            GameHeaderView(
                game = game,
                currentPlayer = state.currentPlayer,
                showBolts = game.isFinished().not(),
                isOnlineGame = state.isOnlineGame,
                onlinePlayerIds = state.onlinePlayerIds,
                reactionsByAuthorId = state.reactionsByAuthorId,
                onPlayerClick = onPlayerClick,
            )

            GameField(state, isAnimated, isManualInputEnabled, onAction, onFinishGame)
        }
    }
}

@Composable
private fun GameField(
    state: GameState,
    isAnimated: Boolean,
    isManualInputEnabled: Boolean,
    onAction: (GameAction) -> Unit,
    onFinishGame: () -> Unit,
) {
    val latestAction = rememberUpdatedState(onAction)
    val coordinator = remember { DiceSceneCoordinator { latestAction.value(GameAction.FinishRoll) } }
    val presentation by coordinator.state.collectAsState()
    LaunchedEffect(state.game?.id, state.currentPlayer?.user?.id, state.currentTurn, state.currentRoll, isAnimated) {
        coordinator.acceptSnapshot(state, isAnimated)
    }
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }
    var historyHeight by remember { mutableStateOf(0) }
    var buttonsHeight by remember { mutableStateOf(0) }
    var resultHeight by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val margin = with(density) { 16.dp.toPx() }
    val height = fieldSize.height.coerceAtLeast(1).toFloat()
    val width = fieldSize.width.coerceAtLeast(1).toFloat()
    val top = ((if (state.currentTurn.size > 1) historyHeight else 0) + margin) / height
    val bottom = (height - buttonsHeight - (if (presentation.phase == DiceScenePhase.Fallback) resultHeight else 0) - margin) / height
    val bounds = DiceSceneBounds(
        left = (margin / width).coerceIn(0f, .4f),
        top = top.coerceIn(0f, .85f),
        right = (1f - margin / width).coerceIn(.6f, 1f),
        bottom = bottom.coerceIn((top + .05f).coerceAtMost(.9f), 1f),
    )
    val color = MaterialTheme.colorScheme.surfaceVariant.toArgb().toUInt().toString(16).padStart(8, '0').takeLast(6)
    val sceneConfiguration = DiceSceneConfiguration(
        presentation.request, "#$color", bounds, presentation.active,
        isOpponentTurn = if (state.isOnlineGame) state.isOpponentTurn else state.currentPlayer?.isMain() == false,
    )
    Box(Modifier.fillMaxSize().onSizeChanged { fieldSize = it }, contentAlignment = Alignment.Center) {
        DiceSceneSurface(
            coordinator, presentation,
            sceneConfiguration,
            Modifier.fillMaxSize().semantics {
                if (presentation.showResult) {
                    contentDescription = listOfNotNull(
                        state.currentRoll?.dice?.joinToString(", ") { it.value.value.toString() },
                        presentation.request?.scoreText,
                        sceneConfiguration.promptText,
                    ).joinToString(". ")
                }
            },
        )
        if (presentation.phase == DiceScenePhase.Fallback) {
            state.currentRoll?.let { roll ->
                Box(
                    Modifier.fillMaxSize().padding(
                        start = 16.dp, end = 16.dp,
                        top = with(density) { (bounds.top * height).toDp() },
                        bottom = with(density) { ((1f - bounds.bottom) * height).toDp() },
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        roll.dice.forEachIndexed { index, die ->
                            val highlighted = index in presentation.request?.scoringIndices.orEmpty()
                            SingleDieView(
                                Modifier.size(48.dp).drawBehind {
                                    if (highlighted) {
                                        val turquoise = Color(0xFF55E5DC)
                                        listOf(8.dp to .08f, 4.dp to .18f, 1.5.dp to .65f).forEach { (spread, alpha) ->
                                            val inset = spread.toPx()
                                            drawRoundRect(
                                                color = turquoise.copy(alpha = alpha),
                                                topLeft = Offset(-inset / 2, -inset / 2),
                                                size = Size(size.width + inset, size.height + inset),
                                                cornerRadius = CornerRadius(5.dp.toPx() + inset / 2),
                                                style = Stroke(inset),
                                            )
                                        }
                                    }
                                },
                                die.value,
                            )
                        }
                    }
                }
            }
        }
        if (state.currentRoll == null) Text("Вы в игре!")
        if (state.currentTurn.size > 1) {
            TurnHistoryView(
                Modifier.align(Alignment.TopStart).onSizeChanged { historyHeight = it.height }.padding(16.dp),
                state.currentTurn,
            )
        }
        if (presentation.phase == DiceScenePhase.Fallback && state.currentRoll != null) {
            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(bottom = with(density) { buttonsHeight.toDp() })
                    .onSizeChanged { resultHeight = it.height }
                    .alpha(if (presentation.showResult) 1f else 0f)
                    .then(if (presentation.showResult) Modifier else Modifier.clearAndSetSemantics {})
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(presentation.request?.scoreText.orEmpty(), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text(sceneConfiguration.promptText, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
        val hideActions = !sceneConfiguration.isOpponentTurn && !presentation.actionsEnabled
        ButtonsView(
            Modifier.align(Alignment.BottomCenter)
                .onSizeChanged { buttonsHeight = it.height }
                .alpha(if (hideActions) 0f else 1f)
                .then(if (hideActions) Modifier.clearAndSetSemantics {} else Modifier),
            state, isManualInputEnabled,
            onAction = { action ->
                if (action == GameAction.FinishTurn) coordinator.clearCompletedRoll()
                onAction(action)
            },
            onFinishGame = onFinishGame,
            actionsEnabled = presentation.actionsEnabled,
        )
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
    onFinishGame: () -> Unit,
    actionsEnabled: Boolean,
) {
    var isManualInputShown by remember { mutableStateOf(false) }
    val shouldUseManualInput = isManualInputEnabled && state.isTutorial.not()
    val tutorialNextAction = state.tutorialNextAction
    val isFinishTurnEnabled = if (state.isTutorial && tutorialNextAction != null) tutorialNextAction == TutorialNextAction.FinishTurn else true
    val isRollEnabled = if (state.isTutorial && tutorialNextAction != null) tutorialNextAction == TutorialNextAction.Reroll else true

    if (isManualInputShown) {
        ModalBottomSheet(
            onDismissRequest = {
                isManualInputShown = false
            }
        ) {
            ManualDiceInputView(
                count = state.rollAbility.count
            ) {
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
        AnimatedVisibility(state.tutorialAdvice != null) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = state.tutorialAdvice ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.buttons.forEach { button ->
                when (button) {
                    GameButton.FINISH_GAME -> {
                        AppButton(
                            style = AppButtonStyle.Secondary,
                            modifier = Modifier.weight(1f),
                            enabled = actionsEnabled,
                            onClick = {
                                onFinishGame()
                            },
                        ) {
                            Text("Закончить игру")
                        }
                    }
                    GameButton.FINISH_TURN -> {
                        AppButton(
                            style = AppButtonStyle.Secondary,
                            modifier = Modifier.weight(1f),
                            enabled = actionsEnabled && isFinishTurnEnabled,
                            onClick = {
                                onAction(GameAction.FinishTurn)
                            },
                        ) {
                            Text("Закончить ход")
                        }
                    }
                    GameButton.ROLL_THE_DICE -> {
                        AppButton(
                            style = AppButtonStyle.Primary,
                            modifier = Modifier.weight(1f),
                            enabled = actionsEnabled && isRollEnabled,
                            onClick = {
                                if (shouldUseManualInput) {
                                    isManualInputShown = true
                                } else {
                                    onAction(GameAction.RollDice)
                                }
                            },
                        ) {
                            Text("Бросить кубики")
                        }
                    }
                    GameButton.ROLL_AGAIN -> {
                        AppButton(
                            style = AppButtonStyle.Primary,
                            modifier = Modifier.weight(1f),
                            enabled = actionsEnabled && isRollEnabled,
                            onClick = {
                                if (shouldUseManualInput) {
                                    isManualInputShown = true
                                } else {
                                    onAction(GameAction.RollDice)
                                }
                            },
                        ) {
                            Text("Перебросить (${state.rollAbility.count})")
                        }
                    }
                    GameButton.BOT_TURN -> {
                        AppButton(
                            style = AppButtonStyle.Secondary,
                            modifier = Modifier.weight(1f),
                            enabled = actionsEnabled,
                            onClick = {
                                onAction(GameAction.BotTurn)
                            },
                        ) {
                            Text("Ход оппонента")
                        }
                    }
                }
            }
        }
    }
}
