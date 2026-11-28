package com.alextos.thousand.presentation.menu.multiplayer.multiplayer_game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.models.GameButton
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.UserReaction
import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.service.GamePresenceObserver
import com.alextos.thousand.domain.service.GamePresenceObserverDelegate
import com.alextos.thousand.domain.service.AchievementService
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
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
    shakeDeviceObserver: ShakeDeviceObserver,
    private val gamePresenceObserver: GamePresenceObserver,
    private val accountService: NativeAccountService,
    private val multiplayerRepository: MultiplayerRepository,
    private val rollTheDice: RollTheDiceUseCase,
    private val calculateDiceRollScore: CalculateDiceRollScoreUseCase,
    private val determineAvailableButtons: DetermineAvailableButtonsUseCase,
    private val saveTurn: SaveTurnUseCase,
    private val formatTurnEffect: FormatTurnEffectUseCase,
    private val updateGame: UpdateGameUseCase,
    private val finishRemoteGame: FinishRemoteGameUseCase,
    private val achievementService: AchievementService,
) : ViewModel(), ShakeDeviceObserverDelegate, GamePresenceObserverDelegate {
    private val gameId = savedStateHandle.toRoute<MultiplayerRoute.MultiplayerGame>().gameId

    private val _state = MutableStateFlow(MultiplayerGameState(gameCode = gameId))
    val state: StateFlow<MultiplayerGameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MultiplayerGameEvent>()
    val events = _events.asSharedFlow()

    private var rollBlocked = false
    private var remoteGame: RemoteGame? = null
    private var pendingGameFinish: RemoteGame? = null
    private var isUserAwayFromGame = false
    private val shownReactionIds = mutableSetOf<String>()

    init {
        shakeDeviceObserver.delegate = this
        gamePresenceObserver.delegate = this
        viewModelScope.launch {
            multiplayerRepository
                .observeGame(gameId)
                .catch { error ->
                    _state.update {
                        it.copy(error = error.message)
                    }
                }
                .collect { game ->
                    if (remoteGame == null) {
                        updateCurrentUserPresence(true)
                    }
                    showMessagesIfNeeded(game.messagesToShow)
                    showReactionIfNeeded(game.reaction)
                    val previous = remoteGame
                    handleGameUpdate(game)
                    checkObservedAchievements(previous, game)
                }
        }
    }

    override fun deviceDidShake() {
        val isCurrentUser = accountService.userProfile.value?.id == remoteGame?.currentPlayer()?.user?.id
        if (isCurrentUser) {
            rollDice()
        }
    }

    override fun userDidLeaveGame() {
        if (isUserAwayFromGame) {
            return
        }

        isUserAwayFromGame = true
        updateCurrentUserPresence(isOnline = false)
    }

    override fun userDidReturnToGame() {
        if (isUserAwayFromGame.not()) {
            return
        }

        isUserAwayFromGame = false
        updateCurrentUserPresence(isOnline = true)
    }

    override fun onCleared() {
        if (gamePresenceObserver.delegate === this) {
            gamePresenceObserver.delegate = null
        }
        super.onCleared()
    }

    fun onAction(action: MultiplayerGameAction) {
        when (action) {
            MultiplayerGameAction.DeleteGame -> deleteGame()
            MultiplayerGameAction.Rematch -> rematch()
            MultiplayerGameAction.DismissGameResultSheet -> dismissGameResultSheet()
            MultiplayerGameAction.LeaveGameScreen -> gamePresenceObserver.notifyUserLeftGame()
            is MultiplayerGameAction.SendGameAction -> reduceGameAction(action.action)
            is MultiplayerGameAction.SendReaction -> sendReaction(action.reaction)
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

    private fun showReactionIfNeeded(reaction: UserReaction?) {
        val reaction = reaction ?: return

        if (shownReactionIds.add(reaction.id).not()) {
            return
        }

        viewModelScope.launch {
            _events.emit(MultiplayerGameEvent.ShowMessage("${reaction.author}: ${reaction.reaction}", isReaction = true))
        }
    }

    private fun handleGameUpdate(game: RemoteGame) {
        val previous = remoteGame
        if (previous?.currentTurn != game.currentTurn || previous.currentPlayer()?.user?.id != game.currentPlayer()?.user?.id) {
            rollBlocked = game.currentRoll != null
        }
        remoteGame = game
        val actualGame = remoteGame ?: game
        val gameState = mapToGameState(actualGame)
        _state.update {
            it.copy(
                isHost = actualGame.host == accountService.userProfile.value?.id,
                gameCode = actualGame.id.toString(),
                gameState = gameState,
                gameResultSheet = actualGame.finishedGameResultSheet(),
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
            buttons = if (isCurrentUser) game.buttons else emptyList(),
            isOnlineGame = true,
            isOpponentTurn = !isCurrentUser,
            onlinePlayerIds = game.onlinePlayerIds,
            reactionsByAuthorId = game.reaction?.takeIf { reaction ->
                reaction.authorId.isNotBlank()
            }?.let { reaction ->
                mapOf(reaction.authorId to reaction)
            } ?: emptyMap(),
        )
    }

    private fun updateCurrentUserPresence(isOnline: Boolean) {
        viewModelScope.launch {
            try {
                multiplayerRepository.updatePlayerOnlineStatus(
                    gameKey = gameId,
                    isOnline = isOnline,
                )
            } catch (e: Exception) {
                _events.emit(MultiplayerGameEvent.Error(e.message ?: "Ошибка при обновлении статуса игрока"))
            }
        }
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
        if (pendingGameFinish != null) return
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
                    gameResultSheet = null
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

            updateRemote(resetGame.startAchievementRound())
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
                applyDiceRoll(rollTheDice(count))
            }
        }
    }

    private fun applyDiceRoll(dice: List<Die>) {
        if (rollBlocked || state.value.gameState.rollAbility == RollAbility.UNAVAILABLE) return
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
            try {
                multiplayerRepository.updateGame(newGame)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                rollBlocked = false
                _events.emit(MultiplayerGameEvent.Error(e.message ?: "Ошибка при обновлении игры"))
                return@launch
            }
            checkAchievements { accountId ->
                achievementService.onOnlineRollSaved(currentPlayer.user.id, accountId, roll, currentTurn.size)
            }
        }
    }

    private fun finishRoll() {
        rollBlocked = false
    }

    private suspend fun finishTurn() {
        if (pendingGameFinish != null || rollBlocked || GameButton.FINISH_TURN !in state.value.gameState.buttons) return
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
            GameStatus.ONGOING -> continueGame(game, messages, turn)
            GameStatus.FINISHED -> finishGame(game, messages, turn)
        }
    }

    private suspend fun continueGame(game: Game, messages: List<String>, turn: Turn) {
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
        val completedGame = newGame.withCompletedAchievementTurn(turn, nextPlayerIndex)
        if (updateRemote(completedGame)) {
            checkAchievements { accountId -> achievementService.onOnlineTurnSaved(completedGame, accountId) }
        }
    }

    private suspend fun finishGame(game: Game, messages: List<String>, turn: Turn) {
        val remoteGame = remoteGame ?: return
        finishRemote(
            remoteGame.copy(
                players = game.players,
                rollAbility = RollAbility.UNAVAILABLE,
                currentRoll = null,
                currentTurn = emptyList(),
                currentPlayerIndex = -1,
                buttons = emptyList(),
                messagesToShow = messages,
                reaction = null,
            ).withCompletedAchievementTurn(turn, nextPlayerIndex = -1),
        )
    }

    private fun toggleNotifications(isEnabled: Boolean) {
        _state.update {
            it.copy(isNotificationEnabled = isEnabled)
        }
    }

    private fun sendReaction(reaction: String) {
        if (state.value.isReactionButtonEnabled.not()) {
            return
        }

        val remoteGame = remoteGame ?: return
        val userProfile = accountService.userProfile.value
        val userReaction = UserReaction(
            authorId = userProfile?.id.orEmpty(),
            author = userProfile?.name.orEmpty(),
            reaction = reaction,
        )

        viewModelScope.launch {
            _state.update {
                it.copy(isReactionButtonEnabled = false)
            }

            try {
                multiplayerRepository.sendReaction(remoteGame.key, userReaction)
            } catch (e: Exception) {
                _events.emit(MultiplayerGameEvent.Error(e.message ?: "Ошибка при отправке реакции"))
            }
            delay(REACTION_COOLDOWN_MILLIS)

            _state.update {
                it.copy(isReactionButtonEnabled = true)
            }
        }
    }

    private suspend fun updateRemote(game: RemoteGame): Boolean {
        try {
            multiplayerRepository.updateGame(game)
            return true
        } catch (e: Exception) {
            _events.emit(MultiplayerGameEvent.Error(e.message ?: "Ошибка при обновлении игры"))
            return false
        }
    }

    private suspend fun finishRemote(game: RemoteGame) {
        pendingGameFinish = game
        try {
            finishRemoteGame(game) { savedResult ->
                checkAchievements { accountId ->
                    achievementService.onOnlineGameFinished(savedResult.game, accountId)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _events.emit(MultiplayerGameEvent.Error(e.message ?: "Ошибка при завершении игры"))
        } finally {
            if (pendingGameFinish === game) pendingGameFinish = null
        }
    }

    private suspend fun checkObservedAchievements(previous: RemoteGame?, game: RemoteGame) {
        val turn = game.achievementTurn ?: return
        if (game.isFinished() && (game.finishedAt == null || pendingGameFinish != null)) return

        checkAchievements { accountId ->
            if (game.finishedAt != null) {
                achievementService.onOnlineGameFinished(game, accountId)
                return@checkAchievements
            }

            val previousTurn = previous?.achievementTurn
            // Our own writes can appear in the Firebase listener before server acknowledgement.
            if (previous != null && turn.playerId != accountId &&
                (previousTurn == null || (previousTurn.roundId == turn.roundId && turn.number > previousTurn.number))
            ) {
                achievementService.onOnlineTurnSaved(game, accountId)
            }
        }
    }

    private suspend fun checkAchievements(check: suspend (String) -> Unit) {
        val accountId = accountService.userProfile.value?.id ?: return
        try {
            check(accountId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _events.emit(MultiplayerGameEvent.Error(e.message ?: "Не удалось сохранить достижение"))
        }
    }
}

private const val REACTION_COOLDOWN_MILLIS = 10_000L
