package com.alextos.thousand.presentation.game.play_game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.usecase.CalculateDiceRollScoreUseCase
import com.alextos.thousand.domain.usecase.FindCurrentPlayerUseCase
import com.alextos.thousand.domain.usecase.LoadGameTurnsUseCase
import com.alextos.thousand.domain.usecase.LoadGameUseCase
import com.alextos.thousand.domain.usecase.RollTheDiceUseCase
import com.alextos.thousand.domain.usecase.SaveTurnUseCase
import com.alextos.thousand.presentation.game.GameRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val saveTurnUseCase: SaveTurnUseCase
) : ViewModel() {
    private val route = savedStateHandle.toRoute<GameRoute.PlayGame>()

    private val _state = MutableStateFlow(PlayGameState())
    val state: StateFlow<PlayGameState> = _state.asStateFlow()

    fun onAction(action: PlayGameAction) {
        when (action) {
            PlayGameAction.LoadGame -> loadGame()
            PlayGameAction.RollTheDice -> rollTheDice()
            PlayGameAction.FinishTurn -> finishTurn()
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
            val turn = Turn(
                player = player,
                rolls = rolls,
                total = 0,
                effects = emptyList(),
                results = emptyList()
            )
            val turnId = saveTurnUseCase(
                player = player,
                rolls = rolls,
                game = game
            )
            val turns = state.value.turns.toMutableList()
            turns.add(turn.copy(id = turnId))
            _state.update {
                it.copy(
                    rollAbility = RollAbility.REQUIRED,
                    currentRoll = null,
                    currentTurn = emptyList(),
                    turns = turns,
                    currentPlayer = findCurrentPlayerUseCase(game, turns)
                )
            }
        }
    }
}
