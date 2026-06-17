package com.alextos.thousand.presentation.menu.multiplayer.multiplayer_game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.RemoteUserInfo
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.service.DiceHapticsService
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.service.ShakeDeviceObserver
import com.alextos.thousand.domain.service.ShakeDeviceObserverDelegate
import com.alextos.thousand.domain.usecase.game.CalculateDiceRollScoreUseCase
import com.alextos.thousand.domain.usecase.game.DetermineAvailableButtonsUseCase
import com.alextos.thousand.domain.usecase.game.FinishRemoteGameUseCase
import com.alextos.thousand.domain.usecase.game.FormatTurnEffectUseCase
import com.alextos.thousand.domain.usecase.game.RollTheDiceUseCase
import com.alextos.thousand.domain.usecase.game.SaveTurnUseCase
import com.alextos.thousand.domain.usecase.game.crud.UpdateGameUseCase
import com.alextos.thousand.domain.usecase.game.server.GameAction
import com.alextos.thousand.domain.usecase.game.server.GameState
import com.alextos.thousand.domain.usecase.game.server.GameStatus
import com.alextos.thousand.presentation.menu.multiplayer.MultiplayerRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

class MultiplayerGameViewModel(
    savedStateHandle: SavedStateHandle,
    shakeDeviceObserver: ShakeDeviceObserver,
    private val accountService: NativeAccountService,
    private val multiplayerRepository: MultiplayerRepository,
    private val rollTheDice: RollTheDiceUseCase,
    private val calculateDiceRollScore: CalculateDiceRollScoreUseCase,
    private val determineAvailableButtons: DetermineAvailableButtonsUseCase,
    private val saveTurn: SaveTurnUseCase,
    private val formatTurnEffect: FormatTurnEffectUseCase,
    private val updateGame: UpdateGameUseCase,
    private val finishRemoteGame: FinishRemoteGameUseCase,
    private val hapticsService: DiceHapticsService
) : ViewModel(), ShakeDeviceObserverDelegate {
    private val gameId = savedStateHandle.toRoute<MultiplayerRoute.MultiplayerGame>().gameId

    private val _state = MutableStateFlow(MultiplayerGameState(gameCode = gameId))
    val state: StateFlow<MultiplayerGameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MultiplayerGameEvent>()
    val events = _events.asSharedFlow()

    private var rollBlocked = false
    private var remoteGame: RemoteGame? = null
    private var needToRequestUserInfo = true
    private var usersInfoJob: Job? = null
    private val shownReactionIds = mutableSetOf<String>()

    init {
        shakeDeviceObserver.delegate = this
        viewModelScope.launch {
            multiplayerRepository
                .observeGame(gameId)
                .catch { error ->
                    _state.update {
                        it.copy(error = error.message)
                    }
                }
                .collect { game ->
                    showMessagesIfNeeded(game.messagesToShow)
                    showReactionIfNeeded(game)
                    handleGameUpdate(game)
                }
        }
    }

    override fun deviceDidShake() {
        val isCurrentUser = accountService.userProfile.value?.id == remoteGame?.currentPlayer()?.user?.id
        if (isCurrentUser) {
            rollDice()
        }
    }

    fun onAction(action: MultiplayerGameAction) {
        when (action) {
            MultiplayerGameAction.DeleteGame -> deleteGame()
            MultiplayerGameAction.Rematch -> rematch()
            MultiplayerGameAction.DismissGameResultSheet -> dismissGameResultSheet()
            is MultiplayerGameAction.SendGameAction -> reduceGameAction(action.action)
            is MultiplayerGameAction.ToggleNotifications -> toggleNotifications(action.isEnabled)
        }
    }

    private fun showMessagesIfNeeded(messages: List<String>) {
        if (state.value.isNotificationEnabled.not()) {
            return
        }

        viewModelScope.launch {
            messages.forEach { message ->
                _events.emit(MultiplayerGameEvent.ShowMessage(message))
            }
        }
    }

    private fun showReactionIfNeeded(game: RemoteGame) {
        val reaction = game.reaction ?: return

        if (shownReactionIds.add(reaction.id).not()) {
            return
        }

        viewModelScope.launch {
            _events.emit(MultiplayerGameEvent.ShowMessage("${reaction.author}: ${reaction.reaction}"))
        }
    }

    private fun handleGameUpdate(game: RemoteGame) {
        remoteGame = game
        loadMissingUsersInfo(game)
        val gameState = mapToGameState(game)
        _state.update {
            it.copy(
                isHost = game.host == accountService.userProfile.value?.id,
                gameCode = game.id.toString(),
                gameState = gameState,
                gameResultSheet = game.finishedGameResultSheet(),
            )
        }
    }

    private fun mapToGameState(game: RemoteGame): GameState {
        val currentPlayer = game.currentPlayer()
        val isCurrentUser = accountService.userProfile.value?.id == currentPlayer?.user?.id
        return GameState(
            isLoading = false,
            game = Game(
                id = game.id,
                settings = game.settings,
                players = game.players,
            ),
            currentPlayer = currentPlayer,
            currentTurn = game.currentTurn,
            currentRoll = game.currentRoll,
            rollAbility = if (isCurrentUser) game.rollAbility else RollAbility.UNAVAILABLE,
            buttons = if (isCurrentUser) game.buttons else emptyList()
        )
    }

    private fun RemoteGame.finishedGameResultSheet(): MultiplayerGameResultSheetUi? {
        if (isFinished().not()) {
            return null
        }

        val winner = players.firstOrNull { it.isWinner } ?: return null
        val currentUserId = accountService.userProfile.value?.id
        return MultiplayerGameResultSheetUi(
            winnerName = winner.user.name,
            isCurrentUser = winner.user.id == currentUserId,
        )
    }

    private fun loadMissingUsersInfo(game: RemoteGame) {
        if (needToRequestUserInfo.not()) {
            return
        }

        val userIds = game.players
            .map { player -> player.user.id }
            .filter { userId -> userId.isNotBlank() }
            .distinct()

        if (userIds.isEmpty()) {
            return
        }

        needToRequestUserInfo = false
        usersInfoJob?.cancel()
        usersInfoJob = viewModelScope.launch {
            userIds
                .map { userId ->
                    multiplayerRepository
                        .observeUserInfo(userId)
                        .map { userInfo -> userInfo.toUserInfoMap(userId) }
                }
                .zip()
                .catch { error ->
                    _state.update {
                        it.copy(error = error.message)
                    }
                }
                .collect { usersInfo ->
                    _state.update {
                        it.copy(usersInfo = usersInfo)
                    }
                }
        }
    }

    private fun deleteGame() {
        viewModelScope.launch {
            try {
                _events.emit(MultiplayerGameEvent.GameDeleted)
                multiplayerRepository.deleteGame(gameId)
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    private fun rematch() {
        viewModelScope.launch {
            val remoteGame = remoteGame ?: return@launch
            val players = remoteGame.players.map { player ->
                player.copy(
                    currentScore = 0,
                    isWinner = false,
                    boltCount = 0,
                    hasPassedStartLimit = false,
                )
            }

            rollBlocked = false
            _state.update {
                it.copy(
                    gameResultSheet = null,
                    usersInfo = emptyMap()
                )
            }
            val resetGame = remoteGame.copy(
                players = players,
                currentPlayerIndex = 0,
                currentTurn = emptyList(),
                currentRoll = null,
                rollAbility = RollAbility.REQUIRED,
                buttons = determineAvailableButtons(
                    currentPlayer = players.firstOrNull(),
                    isFirstRoll = true,
                    isGameOver = false,
                    rollAbility = RollAbility.REQUIRED,
                    isTutorial = false,
                ),
                messagesToShow = emptyList(),
            )

            updateRemote(resetGame)
        }
    }

    private fun dismissGameResultSheet() {
        _state.update {
            it.copy(gameResultSheet = null)
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
        val currentPlayer = remoteGame?.currentPlayer() ?: return
        val roll = DiceRoll(
            dice = dice,
            result = rawResult.score,
        )
        val currentTurn = state.value.gameState.currentTurn.toMutableList()
        currentTurn.add(roll)
        rollBlocked = true
        val newGame = remoteGame?.copy(
            currentTurn = currentTurn,
            currentRoll = roll,
            rollAbility = rawResult.rerollAbility,
            buttons = determineAvailableButtons(
                currentPlayer,
                isFirstRoll = false,
                isGameOver = false,
                rollAbility = rawResult.rerollAbility,
                isTutorial = false
            ),
            messagesToShow = emptyList()
        ) ?: return

        viewModelScope.launch {
            updateRemote(newGame)
        }
    }

    private fun finishRoll() {
        rollBlocked = false
    }

    private suspend fun finishTurn() {
        val remoteGame = remoteGame ?: return
        val game = Game(settings = remoteGame.settings, players = remoteGame.players)
        val player = remoteGame.currentPlayer() ?: return

        val turn = saveTurn(
            currentPlayer = player,
            rolls = state.value.gameState.currentTurn,
            game = game,
            skipSaving = true,
        )

        val messages = turn.effects.map { effect ->
            formatTurnEffect(effect, player, isTutorial = false)
        }

        when (updateGame(game, turn, skipSaving = true)) {
            GameStatus.ONGOING -> continueGame(game, messages)
            GameStatus.FINISHED -> finishGame(game, messages)
        }
    }

    private suspend fun continueGame(game: Game, messages: List<String>) {
        val remoteGame = remoteGame ?: return
        val nextPlayerIndex = (remoteGame.currentPlayerIndex + 1) % remoteGame.players.count()
        val newGame = remoteGame.copy(
            players = game.players,
            currentPlayerIndex = nextPlayerIndex,
            currentTurn = emptyList(),
            currentRoll = null,
            rollAbility = RollAbility.REQUIRED,
            buttons = determineAvailableButtons(
                game.players.getOrNull(nextPlayerIndex),
                isFirstRoll = true,
                isGameOver = false,
                rollAbility = RollAbility.REQUIRED,
                isTutorial = false
            ),
            messagesToShow = messages,
            reaction = null,
        )
        updateRemote(newGame)
    }

    private fun finishGame(game: Game, messages: List<String>) {
        viewModelScope.launch {
            val game = remoteGame?.copy(
                players = game.players,
                rollAbility = RollAbility.UNAVAILABLE,
                currentRoll = null,
                currentTurn = emptyList(),
                currentPlayerIndex = -1,
                buttons = emptyList(),
                messagesToShow = messages,
                reaction = null,
            ) ?: return@launch
            finishRemote(game)
        }
    }

    private fun toggleNotifications(isEnabled: Boolean) {
        _state.update {
            it.copy(isNotificationEnabled = isEnabled)
        }
    }

    private suspend fun updateRemote(game: RemoteGame) {
        try {
            multiplayerRepository.updateGame(game)
        } catch (e: Exception) {
            _events.emit(MultiplayerGameEvent.Error(e.message ?: "Ошибка при обновлении игры"))
        }
    }

    private suspend fun finishRemote(game: RemoteGame) {
        try {
            finishRemoteGame(game, state.value.usersInfo)
        } catch (e: Exception) {
            _events.emit(MultiplayerGameEvent.Error(e.message ?: "Ошибка при завершении игры"))
        }
    }
}

private fun RemoteUserInfo?.toUserInfoMap(userId: String): Map<String, RemoteUserInfo> =
    if (this == null) emptyMap() else mapOf(userId to this)

private fun List<Flow<Map<String, RemoteUserInfo>>>.zip(): Flow<Map<String, RemoteUserInfo>> =
    reduce { accumulatedFlow, nextFlow ->
        accumulatedFlow.zip(nextFlow) { accumulated, next ->
            accumulated + next
        }
    }
