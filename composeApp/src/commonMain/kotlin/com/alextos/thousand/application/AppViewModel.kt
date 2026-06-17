package com.alextos.thousand.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.usecase.user.GetAllUsersUseCase
import com.alextos.thousand.domain.usecase.user.ReplaceMainUserUseCase
import com.alextos.thousand.domain.usecase.user.UpdateUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val nativeAccountService: NativeAccountService,
    private val updateUserUseCase: UpdateUserUseCase,
    private val replaceMainUserUseCase: ReplaceMainUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        observeUsers()
        observeRemoteUser()
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
                nativeAccountService.hideMultiplayer,
            ) { users, hideMultiplayer ->
                users to hideMultiplayer
            }.collect { state ->
                val (users, hideMultiplayer) = state
                _state.update {
                    it.copy(
                        isLoading = false,
                        isOnboardingRequired = users.none { user -> user.kind == UserKind.MainUser },
                        hideMultiplayer = hideMultiplayer,
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

    private fun observeRemoteUser() {
        viewModelScope.launch {
            combine(
                nativeAccountService.userProfile.mapNotNull { it },
                getAllUsersUseCase().mapNotNull { it.firstOrNull { it.kind == UserKind.MainUser } }
            ) { remoteUser, localUser ->
                remoteUser to localUser
            }.collect { (remoteUser, localUser) ->
                if (remoteUser.id != localUser.id) {
                    replaceMainUserUseCase(localUser, remoteUser)
                } else if (remoteUser.name != localUser.name) {
                    updateUserUseCase(localUser.copy(name = remoteUser.name))
                }
            }
        }
    }
}
