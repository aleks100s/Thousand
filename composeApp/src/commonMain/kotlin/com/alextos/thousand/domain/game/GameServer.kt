package com.alextos.thousand.domain.game

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.domain.usecase.game.LoadGameTurnsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameServer(
    private val loadGameTurnsUseCase: LoadGameTurnsUseCase,
    private val findCurrentPlayerUseCase: FindCurrentPlayerUseCase,
    private val rollTheDiceUseCase: RollTheDiceUseCase,
    private val calculateDiceRollScoreUseCase: CalculateDiceRollScoreUseCase,
    private val saveTurnUseCase: SaveTurnUseCase,
    private val updateGameUseCase: UpdateGameUseCase,
) {
    private val _state = MutableStateFlow(GameState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>()
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var rollBlocked = false

    suspend fun initGame(game: Game?) {
        val turns = loadGameTurnsUseCase(game?.id ?: return)
        val currentPlayer = findCurrentPlayerUseCase(game, turns.lastOrNull())
        _state.update {
            GameState(
                isLoading = false,
                game = game,
                currentPlayer = currentPlayer
            )
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

    private fun rollTheDice() {
        if (state.value.rollAbility != RollAbility.UNAVAILABLE && rollBlocked.not()) {
            val count = state.value.rollAbility.count
            _events.tryEmit(GameEvent.HapticFeedback(count))
            val dice = rollTheDiceUseCase(count)
            applyDiceRoll(dice)
        }
    }

    private fun applyDiceRoll(dice: List<Die>) {
        val result = calculateDiceRollScoreUseCase(dice)
        val roll = DiceRoll(dice = dice, result = result.score)
        val currentTurn = state.value.currentTurn.toMutableList()
        currentTurn.add(roll)
        rollBlocked = true
        _state.update {
            it.copy(
                currentTurn = currentTurn,
                currentRoll = roll,
                rollAbility = result.rerollAbility,
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
        val turn = saveTurnUseCase(
            currentPlayer = player,
            rolls = rolls,
            game = game
        )

        if (turn.effects.isNotEmpty()) {
            turn.effects.forEach { effect ->
                _events.tryEmit(GameEvent.Notification(effect.text(player)))
            }
        }

        val status = updateGameUseCase(game, turn)
        _state.update {
            when (status) {
                GameStatus.ONGOING -> {
                    it.copy(
                        rollAbility = RollAbility.REQUIRED,
                        currentRoll = null,
                        currentTurn = emptyList(),
                        currentPlayer = findCurrentPlayerUseCase(game, turn)
                    )
                }
                GameStatus.FINISHED -> {
                    it.copy(
                        rollAbility = RollAbility.UNAVAILABLE,
                        currentRoll = null,
                        currentTurn = emptyList(),
                        currentPlayer = null
                    )
                }
            }
        }
    }
}