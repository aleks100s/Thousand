package com.alextos.thousand.domain.game.server

import com.alextos.thousand.domain.game.ApplyDiceRollRestrictionsUseCase
import com.alextos.thousand.domain.game.CalculateDiceRollScoreUseCase
import com.alextos.thousand.domain.game.FindCurrentPlayerUseCase
import com.alextos.thousand.domain.game.FormatTurnEffectUseCase
import com.alextos.thousand.domain.game.MakeBotReplyUseCase
import com.alextos.thousand.domain.game.MakeBotRollUseCase
import com.alextos.thousand.domain.game.RollTheDiceUseCase
import com.alextos.thousand.domain.game.SaveTurnUseCase
import com.alextos.thousand.domain.game.UpdateGameUseCase
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.Game
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

class GameServer(
    private val loadGameTurns: LoadGameTurnsUseCase,
    private val findCurrentPlayer: FindCurrentPlayerUseCase,
    private val rollTheDice: RollTheDiceUseCase,
    private val calculateDiceRollScore: CalculateDiceRollScoreUseCase,
    private val applyDiceRollRestrictions: ApplyDiceRollRestrictionsUseCase,
    private val saveTurn: SaveTurnUseCase,
    private val updateGame: UpdateGameUseCase,
    private val makeBotRoll: MakeBotRollUseCase,
    private val makeBotReply: MakeBotReplyUseCase,
    private val formatTurnEffect: FormatTurnEffectUseCase,
) {
    private val _state = MutableStateFlow(GameState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>()
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var rollBlocked = false
    private var isTutorial = false

    suspend fun initGame(game: Game?, isTutorial: Boolean = false) {
        this.isTutorial = isTutorial
        val turns = loadGameTurns(game?.id ?: return)
        val currentPlayer = findCurrentPlayer(game, turns.lastOrNull())
        _state.update {
            GameState(
                isLoading = false,
                game = game,
                currentPlayer = currentPlayer,
                isTutorial = isTutorial
            )
        }
        if (currentPlayer?.isBot() == true) {
            makeBotTurn()
        }
        if (isTutorial) {
            val message = "Начнем игру! Цель игры - первым набрать 1000 очков.\n" +
                    "Очки приносят комбинации выброшенных кубиков, полный перечень комбинаций можно найти в разделе правил.\n" +
                    "Первый ход за тобой, бросай кубики!"
            _events.emit(GameEvent.Notification(message))
        }
    }

    suspend fun sendAction(action: GameAction) {
        when (action) {
            GameAction.RollDice -> rollTheDice()
            is GameAction.ApplyRoll -> applyDiceRoll(action.dice)
            GameAction.FinishRoll -> finishRoll()
            GameAction.FinishTurn -> finishTurn()
        }
    }

    private suspend fun rollTheDice() {
        if (state.value.rollAbility != RollAbility.UNAVAILABLE && rollBlocked.not()) {
            val count = state.value.rollAbility.count
            _events.emit(GameEvent.HapticFeedback(count))
            val dice = rollTheDice(count)
            applyDiceRoll(dice)
        }
    }

    private fun applyDiceRoll(dice: List<Die>) {
        val rawResult = calculateDiceRollScore(dice)
        val currentState = state.value
        val currentPlayer = currentState.currentPlayer
        val game = currentState.game
        val turnTotal = currentState.currentTurn.sumOf { it.result } + rawResult.score
        val isFinishTurnBlocked = if (currentPlayer != null && game != null) {
            applyDiceRollRestrictions(
                rerollAbility = rawResult.rerollAbility,
                currentPlayer = currentPlayer,
                game = game,
                turnTotal = turnTotal,
            )
        } else {
            false
        }
        val roll = DiceRoll(
            dice = dice,
            result = rawResult.score,
            rollDescription = if (isTutorial) rawResult.rollDescription else null
        )
        val currentTurn = state.value.currentTurn.toMutableList()
        currentTurn.add(roll)
        rollBlocked = true
        _state.update {
            it.copy(
                currentTurn = currentTurn,
                currentRoll = roll,
                rollAbility = rawResult.rerollAbility,
                isFinishTurnBlocked = isFinishTurnBlocked,
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

        val rolls = state.value.currentTurn
        val turn = saveTurn(
            currentPlayer = player,
            rolls = rolls,
            game = game,
            isTutorial = isTutorial
        )

        if (turn.effects.isNotEmpty()) {
            turn.effects.forEach { effect ->
                _events.emit(GameEvent.Notification(formatTurnEffect(effect, player, isTutorial)))
            }
            if (state.value.currentPlayer?.isBot() == true) {
                _events.emit(GameEvent.Reply(makeBotReply(turn.effects.last().effect)))
            }
        }

        val status = updateGame(game, turn, isTutorial)
        when (status) {
            GameStatus.ONGOING -> {
                continueGame(game, turn)
            }
            GameStatus.FINISHED -> {
                finishGame()
            }
        }
    }

    private suspend fun continueGame(game: Game, turn: Turn) {
        val nextPlayer = findCurrentPlayer(game, turn)
        _state.update {
            it.copy(
                rollAbility = RollAbility.REQUIRED,
                isFinishTurnBlocked = false,
                currentRoll = null,
                currentTurn = emptyList(),
                currentPlayer = nextPlayer
            )
        }
        if (nextPlayer?.isBot() == true) {
            makeBotTurn()
        }
    }

    private fun finishGame() {
        _state.update {
            it.copy(
                rollAbility = RollAbility.UNAVAILABLE,
                isFinishTurnBlocked = false,
                currentRoll = null,
                currentTurn = emptyList(),
                currentPlayer = null
            )
        }
    }

    private suspend fun makeBotTurn() {
        while (true) {
            val value = state.value
            val bot = value.currentPlayer ?: return
            val game = value.game ?: return
            val turnTotal = value.currentTurn.sumOf { it.result }
            if (makeBotRoll(state.value.rollAbility, bot, game, turnTotal)) {
                rollTheDice()
            } else {
                break
            }
        }
        delay(2000L)
        finishTurn()
    }
}
