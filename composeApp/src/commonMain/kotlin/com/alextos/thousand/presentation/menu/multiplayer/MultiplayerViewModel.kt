package com.alextos.thousand.presentation.menu.multiplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.usecase.LogInUseCase
import com.alextos.thousand.domain.usecase.SignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MultiplayerViewModel(
    private val nativeAccountService: NativeAccountService,
    private val logInUseCase: LogInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val multiplayerRepository: MultiplayerRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(MultiplayerState())
    val state: StateFlow<MultiplayerState> = _state.asStateFlow()

    private val _events = Channel<MultiplayerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var joinJob: Job? = null

    init {
        observeUserProfile()
    }

    fun onAction(action: MultiplayerAction) {
        when (action) {
            is MultiplayerAction.OpenInvitation -> openInvitation(action.code, action.id)
            MultiplayerAction.ShowLoginSheet -> showLoginSheet()
            MultiplayerAction.HideLoginSheet -> hideLoginSheet()
            MultiplayerAction.ShowJoinLobbySheet -> showJoinLobbySheet()
            MultiplayerAction.HideJoinLobbySheet -> hideJoinLobbySheet()
            is MultiplayerAction.UpdateLobbyId -> updateLobbyId(action.value)
            is MultiplayerAction.LogIn -> logIn(
                email = action.email,
                password = action.password,
                createAccount = false,
            )
            is MultiplayerAction.SignUp -> logIn(
                email = action.email,
                password = action.password,
                createAccount = true,
            )
            MultiplayerAction.JoinLobby -> joinLobby()
            is MultiplayerAction.DeleteGame -> deleteGame(action.key)
            is MultiplayerAction.DisconnectFromLobby -> disconnectFromLobby(action.key)
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            nativeAccountService.userProfile.collectLatest { userProfile ->
                val isAuthorized = userProfile != null
                _state.update {
                    it.copy(
                        isAuthorized = isAuthorized,
                        currentUserId = userProfile?.id,
                        username = userProfile?.name,
                        isLoginSheetVisible = if (isAuthorized) false else (savedStateHandle.get<String>("pendingLobby") != null || it.isLoginSheetVisible),
                        isLoginInProgress = if (isAuthorized) false else it.isLoginInProgress,
                    )
                }

                if (isAuthorized.not()) {
                    joinJob?.cancel()
                    _state.update {
                        it.copy(
                            lobbies = emptyList(),
                            activeGames = emptyList(),
                            finishedGames = emptyList(),
                        )
                    }
                    return@collectLatest
                }

                joinPendingInvitation()
                observeLobbies()
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

    private fun showLoginSheet() {
        _state.update {
            it.copy(isLoginSheetVisible = true, loginError = null)
        }
    }

    private fun hideLoginSheet() {
        _state.update {
            it.copy(
                isLoginSheetVisible = false,
                isLoginInProgress = false,
            )
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
                canJoinLobby = value.trim().isNotBlank(),
                lobbyError = null
            )
        }
    }

    private fun openInvitation(code: String, id: Long) {
        if (savedStateHandle.get<Long>("handledInvitation") == id) return
        savedStateHandle["handledInvitation"] = id
        savedStateHandle["pendingLobby"] = code
        updateLobbyId(code)
        if (state.value.isAuthorized) joinPendingInvitation() else showLoginSheet()
    }

    private fun joinPendingInvitation() {
        val code = savedStateHandle.get<String>("pendingLobby") ?: return
        updateLobbyId(code)
        joinLobby()
    }

    private fun joinLobby() {
        val lobbyId = state.value.lobbyId.trim()
        if (lobbyId.isBlank() || !state.value.isAuthorized || joinJob?.isActive == true) return
        joinJob = viewModelScope.launch {
            try {
                val key = multiplayerRepository.joinLobby(lobbyId)
                check(key.isNotBlank()) { "Не удалось подключиться к лобби" }
                _state.update { it.copy(isJoinLobbySheetVisible = false, lobbyId = "") }
                savedStateHandle.remove<String>("pendingLobby")
                _events.send(MultiplayerEvent.OpenLobby(key))
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                savedStateHandle.remove<String>("pendingLobby")
                _state.update { it.copy(lobbyError = e.message, isJoinLobbySheetVisible = true) }
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

    private fun logIn(
        email: String,
        password: String,
        createAccount: Boolean,
    ) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank() || state.value.isLoginInProgress) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoginInProgress = createAccount.not(),
                    isSignUpInProgress = createAccount,
                    loginError = null,
                )
            }

            try {
                if (createAccount) {
                    signUpUseCase(
                        email = trimmedEmail,
                        password = password,
                    )
                } else {
                    logInUseCase(
                        email = trimmedEmail,
                        password = password,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loginError = e.message)
                }
            } finally {
                _state.update {
                    it.copy(isLoginInProgress = false, isSignUpInProgress = false)
                }
            }
        }
    }
}
