package com.alextos.thousand.presentation.game.play_game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.game.server.GameAction
import com.alextos.thousand.domain.game.server.GameEvent
import com.alextos.thousand.domain.game.server.GameServer
import com.alextos.thousand.domain.service.DiceHapticsService
import com.alextos.thousand.domain.service.ShakeDeviceObserver
import com.alextos.thousand.domain.service.ShakeDeviceObserverDelegate
import com.alextos.thousand.domain.usecase.game.LoadGameUseCase
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
    private val gameServer: GameServer,
    private val hapticsService: DiceHapticsService,
    shakeDeviceObserver: ShakeDeviceObserver
) : ViewModel(), ShakeDeviceObserverDelegate {
    private val route = savedStateHandle.toRoute<GameRoute.PlayGame>()

    private val _state = MutableStateFlow(PlayGameState())
    val state: StateFlow<PlayGameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PlayGameEvent>()
    val events: SharedFlow<PlayGameEvent> = _events.asSharedFlow()

    private var isShakeEnabled: Boolean = true

    init {
        shakeDeviceObserver.delegate = this

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
                            showMessage(event.message)
                        }

                        is GameEvent.FinishGame -> {
                            _events.emit(PlayGameEvent.FinishGame(event.game))
                        }

                        is GameEvent.HapticFeedback -> {
                            hapticsService.playDiceRollSequence(event.count)
                        }
                    }
                }
        }
    }

    override fun deviceDidShake() {
        if (isShakeEnabled && state.value.isManualInputEnabled.not()) {
            sendGameAction(GameAction.RollDice)
        }
    }

    fun onAction(action: PlayGameAction) {
        when (action) {
            PlayGameAction.LoadGame -> loadGame()
            PlayGameAction.FinishGame -> finishGame()
            is PlayGameAction.SendGameAction -> sendGameAction(action.action)
            is PlayGameAction.ApplyDiceRoll -> sendGameAction(GameAction.ApplyRoll(action.dice))
            is PlayGameAction.SetNotificationEnabled -> setNotificationEnabled(action.isEnabled)
        }
    }

    private fun loadGame() {
        viewModelScope.launch {
            val game = loadGameUseCase(route.gameId)
            _state.update {
                it.copy(
                    isManualInputEnabled = game?.isVirtualDiceEnabled?.not() ?: false,
                    isNotificationEnabled = game?.isNotificationEnabled ?: true,
                )
            }
            isShakeEnabled = game?.isShakeEnabled ?: true
            gameServer.initGame(game)
        }
    }

    private fun sendGameAction(action: GameAction) {
        viewModelScope.launch {
            gameServer.sendAction(action)
        }
    }

    private fun showMessage(message: String) {
        if (state.value.isNotificationEnabled.not()) {
            return
        }

        viewModelScope.launch {
            _events.emit(PlayGameEvent.ShowMessage(message))
        }
    }

    private fun finishGame() {
        viewModelScope.launch {
            _state.value.gameState.game?.let {
                _events.emit(PlayGameEvent.FinishGame(it))
            }
        }
    }

    private fun setNotificationEnabled(isEnabled: Boolean) {
        _state.update {
            it.copy(isNotificationEnabled = isEnabled)
        }
    }
}
