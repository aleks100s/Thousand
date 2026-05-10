package com.alextos.thousand.presentation.game.tutorial_game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.game.server.GameAction
import com.alextos.thousand.domain.game.server.GameEvent
import com.alextos.thousand.domain.game.server.GameServer
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.service.DiceHapticsService
import com.alextos.thousand.domain.usecase.game.GetAllUsersUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TutorialGameViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val gameServer: GameServer,
    private val hapticsService: DiceHapticsService,
) : ViewModel() {
    private val _state = MutableStateFlow(TutorialGameState())
    val state: StateFlow<TutorialGameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TutorialGameEvent>()
    val events: SharedFlow<TutorialGameEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            gameServer.state
                .collect { gameState ->
                    _state.update { it.copy(gameState = gameState) }
                }
        }

        viewModelScope.launch {
            gameServer.events
                .collect { event ->
                    when (event) {
                        is GameEvent.Notification -> {
                            _state.update {
                                it.copy(messageToShow = event.message)
                            }
                        }

                        is GameEvent.HapticFeedback -> {
                            hapticsService.playDiceRollSequence(event.count)
                        }

                        else -> Unit
                    }
                }
        }
    }

    fun onAction(action: TutorialGameAction) {
        when (action) {
            TutorialGameAction.Initialize -> initialize()
            is TutorialGameAction.SendGameAction -> sendGameAction(action.action)
            TutorialGameAction.CloseMessage -> {
                _state.update {
                    it.copy(messageToShow = null)
                }
            }
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            val currentUser = getAllUsersUseCase().firstOrNull()?.firstOrNull() ?: return@launch
            val currentPlayer = Player(user = currentUser)
            val user = User(name = "Оппонент")
            val player = Player(user = user)
            val game = Game(players = listOf(currentPlayer, player))
            gameServer.initGame(game, isTutorial = true)
        }
    }

    private fun sendGameAction(action: GameAction) {
        viewModelScope.launch {
            gameServer.sendAction(action)
        }
    }
}
