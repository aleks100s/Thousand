package com.alextos.thousand.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.repository.MultiplayerManager
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
    private val multiplayerManager: MultiplayerManager
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
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            nativeAccountService.userProfile.collect { userProfile ->
                val isAuthorized = userProfile != null
                _state.update {
                    it.copy(
                        isAuthorized = isAuthorized,
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
               multiplayerManager.userLobbies(),
               multiplayerManager.userGames()
           ) { lobbies, games ->
               lobbies to games
           }
                .catch {
                    _state.update {
                        it.copy(lobbies = emptyList(), games = emptyList())
                    }
                }
                .collect { pair ->
                    _state.update {
                        it.copy(lobbies = pair.first, games = pair.second)
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
                multiplayerManager.joinLobby(lobbyId)
                _state.update {
                    it.copy(isJoinLobbySheetVisible = false, lobbyId = "")
                }
                _events.emit(MultiplayerEvent.OpenLobby(lobbyId))
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
                username = null,
                lobbies = emptyList(),
                isLogoutSheetVisible = false
            )
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
