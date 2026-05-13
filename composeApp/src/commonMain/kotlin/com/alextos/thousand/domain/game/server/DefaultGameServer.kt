package com.alextos.thousand.domain.game.server

import com.alextos.thousand.domain.game.BotDecision
import com.alextos.thousand.domain.game.CalculateDiceRollScoreUseCase
import com.alextos.thousand.domain.game.DetermineAvailableButtonsUseCase
import com.alextos.thousand.domain.game.FindCurrentPlayerUseCase
import com.alextos.thousand.domain.game.FormatTurnEffectUseCase
import com.alextos.thousand.domain.game.MakeBotReplyUseCase
import com.alextos.thousand.domain.game.MakeBotDecisionUseCase
import com.alextos.thousand.domain.game.RollTheDiceUseCase
import com.alextos.thousand.domain.game.SaveTurnUseCase
import com.alextos.thousand.domain.game.UpdateGameUseCase
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.GameButton
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.usecase.game.LoadGameTurnsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DefaultGameServer(
    private val loadGameTurns: LoadGameTurnsUseCase,
    private val findCurrentPlayer: FindCurrentPlayerUseCase,
    private val rollTheDice: RollTheDiceUseCase,
    private val calculateDiceRollScore: CalculateDiceRollScoreUseCase,
    private val saveTurn: SaveTurnUseCase,
    private val updateGame: UpdateGameUseCase,
    private val makeBotRoll: MakeBotDecisionUseCase,
    private val makeBotReply: MakeBotReplyUseCase,
    private val formatTurnEffect: FormatTurnEffectUseCase,
    private val determineAvailableButtons: DetermineAvailableButtonsUseCase
) : GameServer {
    private val _state = MutableStateFlow(GameState())
    override val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>()
    override val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var rollBlocked = false

    override suspend fun initGame(game: Game?) {
        val turns = loadGameTurns(game?.id ?: return)
        val currentPlayer = findCurrentPlayer(game, turns.lastOrNull())
        _state.update {
            GameState(
                isLoading = false,
                game = game,
                currentPlayer = currentPlayer,
                buttons = listOf(GameButton.ROLL_THE_DICE)
            )
        }
        if (currentPlayer?.isBot() == true) {
            makeBotTurn()
        }
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
            applyDiceRoll(rollTheDice(count))
        }
    }

    private fun applyDiceRoll(dice: List<Die>) {
        val rawResult = calculateDiceRollScore(dice)
        val currentState = state.value
        val currentPlayer = currentState.currentPlayer ?: return
        val roll = DiceRoll(
            dice = dice,
            result = rawResult.score,
        )
        val currentTurn = state.value.currentTurn.toMutableList()
        currentTurn.add(roll)
        rollBlocked = true
        _state.update {
            it.copy(
                currentTurn = currentTurn,
                currentRoll = roll,
                rollAbility = rawResult.rerollAbility,
                buttons = determineAvailableButtons(currentPlayer, isFirstRoll = false, isGameOver = false, rollAbility = rawResult.rerollAbility, isTutorial = false)
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
            isTutorial = false,
        )

        turn.effects.forEach { effect ->
            _events.emit(GameEvent.Notification(formatTurnEffect(effect, player, isTutorial = false)))
        }
        if (state.value.currentPlayer?.isBot() == true && turn.effects.isNotEmpty()) {
            _events.emit(GameEvent.Reply(makeBotReply(turn.effects.last().effect)))
        }

        when (updateGame(game, turn, isTutorial = false)) {
            GameStatus.ONGOING -> continueGame(game, turn)
            GameStatus.FINISHED -> finishGame()
        }
    }

    private suspend fun continueGame(game: Game, turn: Turn) {
        val nextPlayer = findCurrentPlayer(game, turn) ?: return
        _state.update {
            it.copy(
                rollAbility = RollAbility.REQUIRED,
                currentRoll = null,
                currentTurn = emptyList(),
                currentPlayer = nextPlayer,
                buttons = determineAvailableButtons(nextPlayer, isFirstRoll = true, isGameOver = false, rollAbility = RollAbility.REQUIRED, isTutorial = false)
            )
        }

        if (nextPlayer.isBot()) {
            makeBotTurn()
        }
    }

    private fun finishGame() {
        _state.update {
            it.copy(
                rollAbility = RollAbility.UNAVAILABLE,
                currentRoll = null,
                currentTurn = emptyList(),
                currentPlayer = null,
                buttons = determineAvailableButtons(isFirstRoll = false, isGameOver = true, rollAbility = RollAbility.REQUIRED, currentPlayer = null, isTutorial = false)
            )
        }
    }

    private suspend fun makeBotTurn() {
        while (true) {
            delay(1500L)
            val value = state.value
            val bot = value.currentPlayer ?: return
            val game = value.game ?: return
            val turnTotal = value.currentTurn.sumOf { it.result }
            if (makeBotRoll(value.rollAbility, bot, game, turnTotal) == BotDecision.CONTINUE) {
                rollTheDice()
            } else {
                break
            }
        }
        delay(2000L)
        finishTurn()
    }
}
