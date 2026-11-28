package com.alextos.thousand.presentation.menu.multiplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.LobbyCode
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.usecase.user.GetAllUsersUseCase
import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.service.NativeAccountService
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class MultiplayerViewModel(
    private val nativeAccountService: NativeAccountService,
    private val multiplayerRepository: MultiplayerRepository,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(MultiplayerState())
    val state: StateFlow<MultiplayerState> = _state.asStateFlow()

    private val _events = Channel<MultiplayerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var connectionJob: Job? = null
    private var joinJob: Job? = null

    init {
        observeAuthorization()
        connect()
    }

    fun onAction(action: MultiplayerAction) {
        when (action) {
            is MultiplayerAction.OpenInvitation -> openInvitation(action.code, action.id)
            MultiplayerAction.ShowJoinLobbySheet -> showJoinLobbySheet()
            MultiplayerAction.HideJoinLobbySheet -> hideJoinLobbySheet()
            is MultiplayerAction.UpdateLobbyId -> updateLobbyId(action.value)
            MultiplayerAction.RetryConnection -> connect()
            MultiplayerAction.JoinLobby -> joinLobby()
            is MultiplayerAction.DeleteGame -> deleteGame(action.key)
            is MultiplayerAction.DisconnectFromLobby -> disconnectFromLobby(action.key)
        }
    }

    private fun connect() {
        if (connectionJob?.isActive == true || state.value.isAuthorized) return

        _state.update { it.copy(isConnecting = true, connectionError = null) }
        connectionJob = viewModelScope.launch {
            try {
                withTimeoutOrNull(15_000) {
                    nativeAccountService.signInAnonymously()
                    combine(nativeAccountService.userId, getAllUsersUseCase()) { userId, users ->
                        users.firstOrNull { it.kind == UserKind.MainUser && it.id == userId }
                    }.first { it != null }
                } ?: error("Подключение заняло слишком много времени")
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update {
                    if (it.isAuthorized) it
                    else it.copy(connectionError = "Не удалось подключиться к мультиплееру. Попробуйте ещё раз.")
                }
            } finally {
                _state.update { it.copy(isConnecting = false) }
            }
        }
    }

    private fun observeAuthorization() {
        viewModelScope.launch {
            combine(nativeAccountService.userId, getAllUsersUseCase()) { userId, users ->
                users.firstOrNull { it.kind == UserKind.MainUser && it.id == userId }?.id
            }
                .distinctUntilChanged()
                .collectLatest { userId ->
                    if (userId == null) {
                        joinJob?.cancel()
                        val wasAuthorized = state.value.isAuthorized
                        _state.update {
                            it.copy(
                                isAuthorized = false,
                                lobbies = emptyList(),
                                activeGames = emptyList(),
                                finishedGames = emptyList(),
                            )
                        }
                        if (wasAuthorized) {
                            _state.update { it.copy(connectionError = "Подключение потеряно. Попробуйте ещё раз.") }
                        }
                    } else {
                        _state.update { it.copy(isAuthorized = true, isConnecting = false, connectionError = null) }
                        joinPendingInvitation()
                        observeLobbies()
                    }
                }
        }
    }

    private suspend fun observeLobbies() {
        combine(
            multiplayerRepository.userLobbies(),
            multiplayerRepository.userGames()
        ) { lobbies, games ->
            lobbies to games
        }
            .catch {
                _state.update {
                    it.copy(
                        lobbies = emptyList(),
                        activeGames = emptyList(),
                        finishedGames = emptyList(),
                    )
                }
            }
            .collect { pair ->
                val games = pair.second
                val activeGames = games.filter { game -> game.isFinished().not() }
                val finishedGames = games.filter { game -> game.isFinished() }

                _state.update {
                    it.copy(
                        lobbies = pair.first,
                        activeGames = activeGames,
                        finishedGames = finishedGames,
                    )
                }
            }
    }

    private fun showJoinLobbySheet() {
        _state.update {
            it.copy(isJoinLobbySheetVisible = true, lobbyError = null)
        }
    }

    private fun hideJoinLobbySheet() {
        _state.update {
            it.copy(isJoinLobbySheetVisible = false, lobbyError = null)
        }
    }

    private fun updateLobbyId(value: String) {
        _state.update {
            it.copy(
                lobbyId = value,
                lobbyError = null
            )
        }
    }

    private fun openInvitation(code: String, id: Long) {
        if (savedStateHandle.get<Long>("handledInvitation") == id) return
        savedStateHandle["handledInvitation"] = id
        savedStateHandle["pendingLobby"] = code
        updateLobbyId(code)
        if (state.value.isAuthorized) joinPendingInvitation()
    }

    private fun joinPendingInvitation() {
        val code = savedStateHandle.get<String>("pendingLobby") ?: return
        updateLobbyId(code)
        joinLobby()
    }

    private fun joinLobby() {
        val lobbyId = LobbyCode.normalize(state.value.lobbyId)
        if (lobbyId.isBlank() || !state.value.isAuthorized || joinJob?.isActive == true) return
        joinJob = viewModelScope.launch {
            _state.update { it.copy(isJoiningLobby = true, lobbyError = null) }
            try {
                val key = withTimeoutOrNull(10_000) { multiplayerRepository.joinLobby(
                        lobbyId,
                        getAllUsersUseCase().first().first { it.kind == UserKind.MainUser && it.id == nativeAccountService.userId.value },
                    ) }
                    ?: error("Не удалось подключиться вовремя. Попробуйте ещё раз.")
                check(key.isNotBlank()) { "Не удалось подключиться к лобби" }
                _state.update { it.copy(isJoinLobbySheetVisible = false, lobbyId = "") }
                savedStateHandle.remove<String>("pendingLobby")
                _events.send(MultiplayerEvent.OpenLobby(key))
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                savedStateHandle.remove<String>("pendingLobby")
                _state.update {
                    it.copy(
                        lobbyError = e.message ?: "Не удалось подключиться к лобби",
                        isJoinLobbySheetVisible = true,
                    )
                }
            } finally {
                _state.update { it.copy(isJoiningLobby = false) }
            }
        }
    }

    private fun deleteGame(key: String) {
        viewModelScope.launch {
            try {
                multiplayerRepository.deleteGame(key)
            } catch (_: Exception) {}
        }
    }

    private fun disconnectFromLobby(key: String) {
        viewModelScope.launch {
            try {
                multiplayerRepository.disconnectFromLobby(key)
            } catch (_: Exception) {}
        }
    }
}
