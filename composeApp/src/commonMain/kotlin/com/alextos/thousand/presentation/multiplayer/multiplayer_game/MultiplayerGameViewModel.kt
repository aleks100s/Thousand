package com.alextos.thousand.presentation.multiplayer.multiplayer_game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.repository.MultiplayerManager
import com.alextos.thousand.domain.service.DiceHapticsService
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.usecase.game.CalculateDiceRollScoreUseCase
import com.alextos.thousand.domain.usecase.game.DetermineAvailableButtonsUseCase
import com.alextos.thousand.domain.usecase.game.RollTheDiceUseCase
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

class MultiplayerGameViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountService: NativeAccountService,
    private val multiplayerManager: MultiplayerManager,
    private val rollTheDice: RollTheDiceUseCase,
    private val calculateDiceRollScore: CalculateDiceRollScoreUseCase,
    private val determineAvailableButtons: DetermineAvailableButtonsUseCase,
    private val hapticsService: DiceHapticsService
) : ViewModel() {
    private val gameId = savedStateHandle.toRoute<MultiplayerRoute.MultiplayerGame>().gameId

    private val _state = MutableStateFlow(MultiplayerGameState(gameCode = gameId),)
    val state: StateFlow<MultiplayerGameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MultiplayerGameEvent>()
    val events = _events.asSharedFlow()

    private var rollBlocked = false
    private var remoteGame: RemoteGame? = null

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
                    handleGameUpdate(game)
                }
        }
    }

    fun onAction(action: MultiplayerGameAction) {
        when (action) {
            MultiplayerGameAction.DeleteGame -> deleteGame()
            is MultiplayerGameAction.SendGameAction -> reduceGameAction(action.action)
        }
    }

    private fun handleGameUpdate(game: RemoteGame) {
        remoteGame = game
        val gameState = mapToGameState(game)
        _state.update {
            it.copy(
                isHost = game.host == accountService.userProfile.value?.id,
                gameCode = game.id.toString(),
                gameState = gameState,
            )
        }
    }

    private fun mapToGameState(game: RemoteGame): GameState {
        val isCurrentPlayer = accountService.userProfile.value?.id == game.currentPlayer?.user?.id
        return GameState(
            isLoading = false,
            game = Game(
                id = game.id,
                settings = game.settings,
                players = game.players,
            ),
            currentPlayer = game.currentPlayer,
            currentTurn = game.currentTurn,
            currentRoll = game.currentRoll,
            rollAbility = if (isCurrentPlayer) game.rollAbility else RollAbility.UNAVAILABLE,
            buttons = if (isCurrentPlayer) game.buttons else emptyList()
        )
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
            is GameAction.ApplyRoll -> applyDiceRoll(action.dice)
            GameAction.FinishRoll -> finishRoll()
            GameAction.FinishTurn -> {
                viewModelScope.launch {
                    finishTurn()
                }
            }
            GameAction.BotTurn -> Unit
        }
    }

    private fun rollDice() {
        viewModelScope.launch {
            if (state.value.gameState.rollAbility != RollAbility.UNAVAILABLE && rollBlocked.not()) {
                val count = state.value.gameState.rollAbility.count
                hapticsService.playDiceRollSequence(count)
                applyDiceRoll(rollTheDice(count))
            }
        }
    }

    private fun applyDiceRoll(dice: List<Die>) {
        val rawResult = calculateDiceRollScore(dice)
        val currentState = state.value
        val currentPlayer = currentState.gameState.currentPlayer ?: return
        val roll = DiceRoll(
            dice = dice,
            result = rawResult.score,
        )
        val currentTurn = state.value.gameState.currentTurn.toMutableList()
        currentTurn.add(roll)
        rollBlocked = true
        val gameState = state.value.gameState
        val newGame = remoteGame?.copy(
            currentPlayer = gameState.currentPlayer,
            currentTurn = currentTurn,
            currentRoll = roll,
            rollAbility = rawResult.rerollAbility,
            buttons = determineAvailableButtons(
                currentPlayer,
                isFirstRoll = false,
                isGameOver = false,
                rollAbility = rawResult.rerollAbility,
                isTutorial = false
            )
        ) ?: return

        viewModelScope.launch {
            multiplayerManager.updateGame(newGame)
        }
    }

    private fun finishRoll() {
        rollBlocked = false
    }

    private suspend fun finishTurn() {
//        val game = state.value.game
//        val player = state.value.currentPlayer
//        if (game == null || player == null) {
//            return
//        }
//
//        val turn = saveTurn(
//            currentPlayer = player,
//            rolls = state.value.currentTurn,
//            game = game,
//            isTutorial = false,
//        )
//
//        turn.effects.forEach { effect ->
//            _events.emit(GameEvent.Notification(formatTurnEffect(effect, player, isTutorial = false)))
//        }
//        if (state.value.currentPlayer?.isBot() == true && turn.effects.isNotEmpty()) {
//            _events.emit(GameEvent.Reply(makeBotReply(turn.effects.last().effect)))
//        }
//
//        when (updateGame(game, turn)) {
//            GameStatus.ONGOING -> continueGame(game, turn)
//            GameStatus.FINISHED -> finishGame()
//        }
    }

//    private fun updateGame(): GameStatus {
//        var status = GameStatus.ONGOING
//        game.players.forEach { player ->
//            val turnResult = currentTurn.results.firstOrNull { it.player == player }
//            if (turnResult != null) {
//                player.isWinner = turnResult.newScore >= GameConstants.GAME_GOAL
//                status = if (player.isWinner) {
//                    GameStatus.FINISHED
//                } else {
//                    GameStatus.ONGOING
//                }
//            }
//        }
//        game.finishedAt = if (status == GameStatus.FINISHED) Clock.System.now() else null
//        repository.saveGame(game)
//        return status
//    }

    private suspend fun continueGame(game: Game, turn: Turn) {
//        val nextPlayer = findCurrentPlayer(game, turn) ?: return
//        _state.update {
//            it.copy(
//                rollAbility = RollAbility.REQUIRED,
//                currentRoll = null,
//                currentTurn = emptyList(),
//                currentPlayer = nextPlayer,
//                buttons = determineAvailableButtons(nextPlayer, isFirstRoll = true, isGameOver = false, rollAbility = RollAbility.REQUIRED, isTutorial = false)
//            )
//        }
//
//        if (nextPlayer.isBot()) {
//            makeBotTurn()
//        }
    }

    private fun finishGame() {
        viewModelScope.launch {
            val game = remoteGame?.copy(
                rollAbility = RollAbility.UNAVAILABLE,
                currentRoll = null,
                currentTurn = emptyList(),
                currentPlayer = null,
                buttons = determineAvailableButtons(
                    isFirstRoll = false,
                    isGameOver = true,
                    rollAbility = RollAbility.REQUIRED,
                    currentPlayer = null,
                    isTutorial = false
                )
            ) ?: return@launch
            multiplayerManager.updateGame(game)
        }
    }
}
