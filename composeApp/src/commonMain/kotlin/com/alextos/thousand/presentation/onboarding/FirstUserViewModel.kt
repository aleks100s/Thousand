package com.alextos.thousand.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.usecase.user.SaveUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class FirstUserViewModel(
    private val saveUserUseCase: SaveUserUseCase,
    private val accountService: NativeAccountService,
) : ViewModel() {
    private val _state = MutableStateFlow(FirstUserState())
    val state: StateFlow<FirstUserState> = _state.asStateFlow()

    init {
        observeAuthorizedUserName()
    }

    private fun observeAuthorizedUserName() {
        viewModelScope.launch {
            accountService.authorizedUserName.collect { name ->
                val authorizedName = name?.trim().orEmpty()
                if (authorizedName.isBlank()) return@collect

                _state.update {
                    if (it.name.isBlank()) {
                        it.copy(name = authorizedName)
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun onAction(action: FirstUserAction) {
        when (action) {
            is FirstUserAction.UpdateName -> updateName(action.value)
            FirstUserAction.SaveUser -> saveUser()
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

            accountService.updatePlayerName(name)
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
}
