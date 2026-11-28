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

class OnboardingViewModel(
    private val saveUserUseCase: SaveUserUseCase,
    private val accountService: NativeAccountService,
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.SelectAvatar -> _state.update { it.copy(avatar = action.avatar) }
            is OnboardingAction.UpdateName -> updateName(action.value)
            OnboardingAction.SaveUser -> saveUser(state.value.name.trim())
        }
    }

    private fun updateName(value: String) {
        _state.update {
            it.copy(name = value)
        }
    }

    private fun saveUser(name: String) {
        if (name.isBlank() || state.value.isSaving) return

        viewModelScope.launch {
            _state.update {
                it.copy(isSaving = true)
            }

            saveUserUseCase(
                name = name,
                kind = UserKind.MainUser,
                id = accountService.userProfile.value?.id,
                avatar = state.value.avatar,
            )

            _state.update { OnboardingState() }
        }
    }
}
