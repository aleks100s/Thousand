package com.alextos.thousand.presentation.game.play_game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.GameStatus
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.domain.service.ShakeDeviceObserver
import com.alextos.thousand.domain.service.ShakeDeviceObserverDelegate
import com.alextos.thousand.domain.usecase.game.CalculateDiceRollScoreUseCase
import com.alextos.thousand.domain.usecase.game.FindCurrentPlayerUseCase
import com.alextos.thousand.domain.usecase.game.LoadGameTurnsUseCase
import com.alextos.thousand.domain.usecase.game.LoadGameUseCase
import com.alextos.thousand.domain.usecase.game.RollTheDiceUseCase
import com.alextos.thousand.domain.usecase.game.SaveTurnUseCase
import com.alextos.thousand.domain.usecase.game.UpdateGameUseCase
import com.alextos.thousand.presentation.game.GameRoute
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayGameViewModel(
    savedStateHandle: SavedStateHandle,
    private val loadGameUseCase: LoadGameUseCase,
    private val loadGameTurnsUseCase: LoadGameTurnsUseCase,
    private val findCurrentPlayerUseCase: FindCurrentPlayerUseCase,
    private val rollTheDiceUseCase: RollTheDiceUseCase,
    private val calculateDiceRollScoreUseCase: CalculateDiceRollScoreUseCase,
    private val saveTurnUseCase: SaveTurnUseCase,
    private val updateGameUseCase: UpdateGameUseCase,
    shakeDeviceObserver: ShakeDeviceObserver
) : ViewModel(), ShakeDeviceObserverDelegate {
    private val route = savedStateHandle.toRoute<GameRoute.PlayGame>()

    private val _state = MutableStateFlow(PlayGameState())
    val state: StateFlow<PlayGameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PlayGameEvent>()
    val events: SharedFlow<PlayGameEvent> = _events.asSharedFlow()

    private var rollBlocked: Boolean = false

    init {
        shakeDeviceObserver.delegate = this
    }

    override fun deviceDidShake() {
        if (state.value.rollAbility != RollAbility.UNAVAILABLE && rollBlocked.not()) {
            rollTheDice()
        }
    }

    fun onAction(action: PlayGameAction) {
        when (action) {
            PlayGameAction.LoadGame -> loadGame()
            PlayGameAction.RollTheDice -> rollTheDice()
            PlayGameAction.FinishTurn -> finishTurn()
            PlayGameAction.FinishRoll -> finishRoll()
        }
    }

    private fun loadGame() {
        viewModelScope.launch {
            val game = loadGameUseCase(route.gameId)
            val turns = loadGameTurnsUseCase(route.gameId)
            val currentPlayer = findCurrentPlayerUseCase(game, turns)
            _state.update {
                it.copy(
                    isLoading = false,
                    game = game,
                    turns = turns,
                    currentPlayer = currentPlayer
                )
            }
        }
    }

    private fun rollTheDice() {
        viewModelScope.launch {
            val dice = rollTheDiceUseCase(state.value.rollAbility.count)
            val result = calculateDiceRollScoreUseCase(dice)
            val roll = DiceRoll(dice = dice, result = result.score)
            val currentTurn = state.value.currentTurn.toMutableList()
            currentTurn.add(roll)
            rollBlocked = true
            _state.update {
                it.copy(
                    currentTurn = currentTurn,
                    currentRoll = roll,
                    rollAbility = result.rerollAbility
                )
            }
        }
    }

    private fun finishTurn() {
        viewModelScope.launch {
            val game = state.value.game
            val player = state.value.currentPlayer
            if (game == null || player == null) {
                return@launch
            }

            val rolls = state.value.currentTurn
            val turn = saveTurnUseCase(
                currentPlayer = player,
                rolls = rolls,
                game = game
            )
            if (turn.effects.isNotEmpty()) {
                turn.effects.forEach { effect ->
                    showSnackbar(effect.text(player))
                }
            }
            val turns = state.value.turns.toMutableList()
            turns.add(turn)
            val status = updateGameUseCase(game, turn)
            _state.update {
                when (status) {
                    GameStatus.ONGOING -> {
                        it.copy(
                            rollAbility = RollAbility.REQUIRED,
                            currentRoll = null,
                            currentTurn = emptyList(),
                            turns = turns,
                            currentPlayer = findCurrentPlayerUseCase(game, turns)
                        )
                    }
                    GameStatus.FINISHED -> {
                        it.copy(
                            rollAbility = RollAbility.UNAVAILABLE,
                            currentRoll = null,
                            currentTurn = emptyList(),
                            turns = turns,
                            currentPlayer = null
                        )
                    }
                }
            }
        }
    }

    private fun finishRoll() {
        rollBlocked = false
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _events.emit(PlayGameEvent.ShowSnackbar(message))
        }
    }
}
