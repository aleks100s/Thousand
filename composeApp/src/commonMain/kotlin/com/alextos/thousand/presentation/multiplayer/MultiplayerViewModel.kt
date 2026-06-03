package com.alextos.thousand.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.usecase.LogInUseCase
import com.alextos.thousand.domain.usecase.SignUpUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MultiplayerViewModel(
    private val nativeAccountService: NativeAccountService,
    private val logInUseCase: LogInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val multiplayerRepository: MultiplayerRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MultiplayerState())
    val state: StateFlow<MultiplayerState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MultiplayerEvent>()
    val events: SharedFlow<MultiplayerEvent> = _events.asSharedFlow()

    init {
        observeUserProfile()
    }

    fun onAction(action: MultiplayerAction) {
        when (action) {
            MultiplayerAction.ShowLoginSheet -> showLoginSheet()
            MultiplayerAction.HideLoginSheet -> hideLoginSheet()
            MultiplayerAction.ShowJoinLobbySheet -> showJoinLobbySheet()
            MultiplayerAction.HideJoinLobbySheet -> hideJoinLobbySheet()
            is MultiplayerAction.UpdateEmail -> updateEmail(action.value)
            is MultiplayerAction.UpdatePassword -> updatePassword(action.value)
            is MultiplayerAction.UpdateLobbyId -> updateLobbyId(action.value)
            MultiplayerAction.LogIn -> logIn(createAccount = false)
            MultiplayerAction.SignUp -> logIn(createAccount = true)
            MultiplayerAction.JoinLobby -> joinLobby()
            MultiplayerAction.SignOut -> signOut()
            MultiplayerAction.ShowLogoutSheet -> showLogout()
            MultiplayerAction.HideLogoutSheet -> hideLogout()
            is MultiplayerAction.DeleteGame -> deleteGame(action.key)
            is MultiplayerAction.DisconnectFromLobby -> disconnectFromLobby(action.key)
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            nativeAccountService.userProfile.collect { userProfile ->
                val isAuthorized = userProfile != null
                _state.update {
                    it.copy(
                        isAuthorized = isAuthorized,
                        currentUserId = userProfile?.id,
                        username = userProfile?.name,
                        isLoginSheetVisible = if (isAuthorized) false else it.isLoginSheetVisible,
                        isLoginInProgress = if (isAuthorized) false else it.isLoginInProgress,
                    )
                }
                if (isAuthorized) {
                    observeLobbies()
                }
            }
        }
    }

    private fun observeLobbies() {
        viewModelScope.launch {
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
    }

    private fun showLoginSheet() {
        _state.update {
            it.copy(isLoginSheetVisible = true)
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
            it.copy(isJoinLobbySheetVisible = true)
        }
    }

    private fun hideJoinLobbySheet() {
        _state.update {
            it.copy(isJoinLobbySheetVisible = false)
        }
    }

    private fun updateEmail(value: String) {
        _state.update {
            it.copy(email = value).withValidation()
        }
    }

    private fun updatePassword(value: String) {
        _state.update {
            it.copy(password = value).withValidation()
        }
    }

    private fun updateLobbyId(value: String) {
        _state.update {
            it.copy(
                lobbyId = value,
                canJoinLobby = value.trim().isNotBlank(),
            )
        }
    }

    private fun joinLobby() {
        val lobbyId = state.value.lobbyId.trim()
        if (lobbyId.isBlank()) return

        viewModelScope.launch {
            try {
                val key = multiplayerRepository.joinLobby(lobbyId)
                _state.update {
                    it.copy(isJoinLobbySheetVisible = false, lobbyId = "")
                }
                _events.emit(MultiplayerEvent.OpenLobby(key))
            } catch (_: Exception) {}
        }
    }

    private fun showLogout() {
        _state.update {
            it.copy(isLogoutSheetVisible = true)
        }
    }

    private fun hideLogout() {
        _state.update {
            it.copy(isLogoutSheetVisible = false)
        }
    }

    private fun signOut() {
        nativeAccountService.signOut()
        _state.update {
            it.copy(
                isAuthorized = false,
                currentUserId = null,
                username = null,
                lobbies = emptyList(),
                activeGames = emptyList(),
                finishedGames = emptyList(),
                isLogoutSheetVisible = false
            )
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

    private fun logIn(createAccount: Boolean) {
        val state = state.value
        if (state.canLogIn.not() || state.isLoginInProgress) return

        viewModelScope.launch {
            _state.update {
                it.copy(isLoginInProgress = createAccount.not(), isSignUpInProgress = createAccount)
            }

            try {
                if (createAccount) {
                    signUpUseCase(
                        email = state.email.trim(),
                        password = state.password,
                    )
                } else {
                    logInUseCase(
                        email = state.email.trim(),
                        password = state.password,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message)
                }
            } finally {
                _state.update {
                    it.copy(isLoginInProgress = false, isSignUpInProgress = false)
                }
            }
        }
    }

    private fun MultiplayerState.withValidation(): MultiplayerState =
        copy(canLogIn = email.trim().isNotBlank() && password.isNotBlank(), error = null)
}
