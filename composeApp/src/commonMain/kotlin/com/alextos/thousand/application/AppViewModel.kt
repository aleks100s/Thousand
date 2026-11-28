package com.alextos.thousand.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.application.invites.LobbyInvitations
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.usecase.user.GetAllUsersUseCase
import com.alextos.thousand.domain.usecase.user.ReplaceMainUserUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val nativeAccountService: NativeAccountService,
    private val replaceMainUserUseCase: ReplaceMainUserUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        observeUsers()
        viewModelScope.launch {
            try {
                nativeAccountService.signInAnonymously()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Local play is available while the next multiplayer entry retries sign-in.
            }
        }
    }

    fun onAction(action: AppAction) {
        when (action) {
            AppAction.LaunchFinished -> launchFinished()
        }
    }

    private fun observeUsers() {
        viewModelScope.launch {
            combine(
                getAllUsersUseCase(),
                nativeAccountService.userProfile.map { it?.id }.distinctUntilChanged(),
            ) { users, accountId ->
                users.firstOrNull { it.kind == UserKind.MainUser } to accountId
            }.collect { (localUser, accountId) ->
                if (localUser != null && accountId != null) {
                    if (localUser.id == accountId) {
                        nativeAccountService.updateLocalAppearance(localUser)
                    } else {
                        replaceMainUserUseCase(localUser, localUser.copy(id = accountId))
                    }
                }
                if (localUser == null && !_state.value.isLoading && !_state.value.isOnboardingRequired) {
                    LobbyInvitations.clear()
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        isOnboardingRequired = localUser == null,
                    )
                }
            }
        }
    }

    private fun launchFinished() {
        _state.update {
            it.copy(isLaunchFinished = true)
        }
    }
}
