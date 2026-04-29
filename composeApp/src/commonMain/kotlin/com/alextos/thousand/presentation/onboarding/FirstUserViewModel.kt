package com.alextos.thousand.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.usecase.game.GetAllUsersUseCase
import com.alextos.thousand.domain.usecase.game.SaveUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class FirstUserViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val saveUserUseCase: SaveUserUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(FirstUserState())
    val state: StateFlow<FirstUserState> = _state.asStateFlow()

    init {
        observeUsers()
    }

    fun onAction(action: FirstUserAction) {
        when (action) {
            is FirstUserAction.UpdateName -> updateName(action.value)
            is FirstUserAction.SelectExistingUser -> selectExistingUser(action.user)
            FirstUserAction.SaveUser -> saveUser()
        }
    }

    private fun observeUsers() {
        viewModelScope.launch {
            getAllUsersUseCase().collect { users ->
                _state.update { it ->
                    it.copy(
                        isLoading = false,
                        isFirstUserRequired = users.none { it.kind == UserKind.MainUser },
                        localUsers = users.filter { it.kind == UserKind.LocalUser },
                    )
                }
            }
        }
    }

    private fun updateName(value: String) {
        _state.update {
            it.copy(name = value)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun saveUser() {
        val name = state.value.name.trim()
        if (name.isBlank() || state.value.isSaving) return

        viewModelScope.launch {
            _state.update {
                it.copy(isSaving = true)
            }

            saveUserUseCase(
                name = name,
                kind = UserKind.MainUser,
                multiplayerToken = Uuid.random().toHexString()
            )

            _state.update {
                it.copy(
                    name = "",
                    isSaving = false,
                )
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun selectExistingUser(user: User) {
        if (state.value.isSaving) return

        viewModelScope.launch {
            _state.update {
                it.copy(isSaving = true)
            }

            saveUserUseCase(
                id = user.id,
                name = user.name,
                kind = UserKind.MainUser,
                multiplayerToken = Uuid.random().toHexString()
            )

            _state.update {
                it.copy(isSaving = false)
            }
        }
    }
}
