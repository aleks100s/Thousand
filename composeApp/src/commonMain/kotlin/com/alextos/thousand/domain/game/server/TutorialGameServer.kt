package com.alextos.thousand.domain.game.server

import com.alextos.thousand.domain.game.ApplyDiceRollRestrictionsUseCase
import com.alextos.thousand.domain.game.CalculateDiceRollScoreUseCase
import com.alextos.thousand.domain.game.DetermineAvailableButtonsUseCase
import com.alextos.thousand.domain.game.FindCurrentPlayerUseCase
import com.alextos.thousand.domain.game.FormatTurnEffectUseCase
import com.alextos.thousand.domain.game.SaveTurnUseCase
import com.alextos.thousand.domain.game.TutorialNextAction
import com.alextos.thousand.domain.game.TutorialRollUseCase
import com.alextos.thousand.domain.game.UpdateGameUseCase
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.GameButton
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.domain.models.Turn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TutorialGameServer(
    private val findCurrentPlayer: FindCurrentPlayerUseCase,
    private val tutorialRoll: TutorialRollUseCase,
    private val calculateDiceRollScore: CalculateDiceRollScoreUseCase,
    private val applyDiceRollRestrictions: ApplyDiceRollRestrictionsUseCase,
    private val saveTurn: SaveTurnUseCase,
    private val updateGame: UpdateGameUseCase,
    private val formatTurnEffect: FormatTurnEffectUseCase,
    private val determineAvailableButtons: DetermineAvailableButtonsUseCase
) : GameServer {
    private val _state = MutableStateFlow(GameState())
    override val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>()
    override val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var rollBlocked = false

    override suspend fun initGame(game: Game?) {
        tutorialRoll.reset()
        val currentPlayer = findCurrentPlayer(game, null)
        _state.update {
            GameState(
                isLoading = false,
                game = game,
                currentPlayer = currentPlayer,
                isTutorial = true,
                buttons = listOf(GameButton.ROLL_THE_DICE)
            )
        }
        if (currentPlayer?.isBot() == true) {
            makeBotTurn()
        }
        _events.emit(
            GameEvent.Notification(
                "Начнем игру! Цель игры - первым набрать 1000 очков.\n" +
                    "Очки приносят комбинации выброшенных кубиков, полный перечень комбинаций можно найти в разделе правил.\n" +
                    "Первый ход за тобой, бросай кубики!"
            )
        )
    }

    override suspend fun sendAction(action: GameAction) {
        when (action) {
            GameAction.RollDice -> rollTheDice()
            is GameAction.ApplyRoll -> applyDiceRoll(action.dice)
            GameAction.FinishRoll -> finishRoll()
            GameAction.FinishTurn -> finishTurn()
            GameAction.BotTurn -> makeBotTurn()
        }
    }

    private suspend fun rollTheDice() {
        if (state.value.rollAbility != RollAbility.UNAVAILABLE && rollBlocked.not()) {
            val count = state.value.rollAbility.count
            _events.emit(GameEvent.HapticFeedback(count))
            val tutorialRollResult = tutorialRoll()
            applyDiceRoll(
                dice = tutorialRollResult.dice,
                tutorialNextAction = tutorialRollResult.nextAction,
                tutorialAdvice = tutorialRollResult.advice,
            )
        }
    }

    private fun applyDiceRoll(
        dice: List<Die>,
        tutorialNextAction: TutorialNextAction? = null,
        tutorialAdvice: String? = null,
    ) {
        val rawResult = calculateDiceRollScore(dice)
        val currentState = state.value
        val currentPlayer = currentState.currentPlayer ?: return
        val roll = DiceRoll(
            dice = dice,
            result = rawResult.score,
            rollDescription = rawResult.rollDescription,
        )
        val currentTurn = state.value.currentTurn.toMutableList()
        currentTurn.add(roll)
        rollBlocked = true
        _state.update {
            it.copy(
                currentTurn = currentTurn,
                currentRoll = roll,
                rollAbility = rawResult.rerollAbility,
                tutorialNextAction = tutorialNextAction,
                tutorialAdvice = tutorialAdvice,
                buttons = determineAvailableButtons(currentPlayer, isFirstRoll = false, isGameOver = false, rollAbility = rawResult.rerollAbility, isTutorial = true)
            )
        }
    }

    private fun finishRoll() {
        rollBlocked = false
    }

    private suspend fun finishTurn() {
        val game = state.value.game
        val player = state.value.currentPlayer
        if (game == null || player == null) {
            return
        }

        val turn = saveTurn(
            currentPlayer = player,
            rolls = state.value.currentTurn,
            game = game,
            isTutorial = true,
        )

        if (turn.effects.isNotEmpty()) {
            if (state.value.currentPlayer?.isBot() == true) {
                _events.emit(
                    GameEvent.Notification(
                        turn.effects.map { effect ->
                            formatTurnEffect(effect, player, isTutorial = true)
                        }.joinToString()
                    )
                )
            }
        }

        when (updateGame(game, turn, isTutorial = true)) {
            GameStatus.ONGOING -> continueGame(game, turn)
            GameStatus.FINISHED -> finishGame()
        }
    }

    private fun continueGame(game: Game, turn: Turn) {
        val nextPlayer = findCurrentPlayer(game, turn) ?: return
        _state.update {
            it.copy(
                rollAbility = RollAbility.REQUIRED,
                currentRoll = null,
                currentTurn = emptyList(),
                currentPlayer = nextPlayer,
                tutorialNextAction = null,
                tutorialAdvice = null,
                buttons = determineAvailableButtons(nextPlayer, isFirstRoll = true, isGameOver = false, rollAbility = RollAbility.REQUIRED, isTutorial = true)
            )
        }
    }

    private fun finishGame() {
        _state.update {
            it.copy(
                rollAbility = RollAbility.UNAVAILABLE,
                currentRoll = null,
                currentTurn = emptyList(),
                currentPlayer = null,
                tutorialNextAction = null,
                tutorialAdvice = null,
                buttons = determineAvailableButtons(isFirstRoll = false, isGameOver = true, rollAbility = RollAbility.REQUIRED, currentPlayer = null, isTutorial = true)
            )
        }
    }

    private suspend fun makeBotTurn() {
        while (true) {
            when (state.value.tutorialNextAction) {
                TutorialNextAction.Reroll -> rollTheDice()
                TutorialNextAction.FinishTurn -> break
                null -> rollTheDice()
            }
            delay(3000L)
        }
        delay(2000L)
        finishTurn()
    }
}
