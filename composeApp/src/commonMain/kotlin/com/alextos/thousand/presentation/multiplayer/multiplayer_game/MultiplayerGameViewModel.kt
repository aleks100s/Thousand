package com.alextos.thousand.presentation.multiplayer.multiplayer_game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.domain.repository.MultiplayerManager
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.usecase.game.server.GameAction
import com.alextos.thousand.domain.usecase.game.server.GameState
import com.alextos.thousand.presentation.multiplayer.MultiplayerRoute
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.text.orEmpty

class MultiplayerGameViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountService: NativeAccountService,
    private val multiplayerManager: MultiplayerManager,
) : ViewModel() {
    private val gameId = savedStateHandle.toRoute<MultiplayerRoute.MultiplayerGame>().gameId

    private val _state = MutableStateFlow(MultiplayerGameState(gameCode = gameId),)
    val state: StateFlow<MultiplayerGameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MultiplayerGameEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            multiplayerManager
                .observeGame(gameId)
                .catch { error ->
                    _state.update {
                        it.copy(error = error.message)
                    }
                }
                .collect { game ->
                    mapToGameState(game)
                }
        }
    }

    fun onAction(action: MultiplayerGameAction) {
        when (action) {
            MultiplayerGameAction.DeleteGame -> deleteGame()
            is MultiplayerGameAction.SendGameAction -> reduceGameAction(action.action)
        }
    }

    private fun mapToGameState(game: RemoteGame) {
        val gameState = GameState(
            isLoading = false,
            game = Game(
                id = game.id,
                settings = game.settings,
                players = game.players,
            ),
            currentPlayer = game.currentPlayer,
            currentTurn = game.currentTurn,
            currentRoll = game.currentRoll,
            rollAbility = RollAbility.REQUIRED,
            buttons = emptyList()
        )
        _state.update {
            it.copy(
                isHost = game.host == accountService.userProfile.value?.id,
                gameCode = game.id.toString(),
                gameState = gameState,
            )
        }
    }

    private fun deleteGame() {
        viewModelScope.launch {
            try {
                _events.emit(MultiplayerGameEvent.GameDeleted)
                multiplayerManager.deleteGame(gameId)
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    private fun reduceGameAction(action: GameAction) {
        when (action) {
            GameAction.RollDice -> rollDice()
            is GameAction.ApplyRoll -> TODO()
            GameAction.FinishRoll -> TODO()
            GameAction.FinishTurn -> TODO()
            GameAction.BotTurn -> Unit
        }
    }

    private fun rollDice() {

    }
}
