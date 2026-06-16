package com.alextos.thousand.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.usecase.user.SaveUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

class OnboardingViewModel(
    private val saveUserUseCase: SaveUserUseCase,
    private val accountService: NativeAccountService,
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        observeAuthorizedUserName()
    }

    private fun observeAuthorizedUserName() {
        viewModelScope.launch {
            accountService.userProfile
                .mapNotNull { it?.name }
                .collect { authorizedName ->
                    _state.update {
                        it.copy(
                            name = authorizedName,
                            isLoginSheetVisible = false,
                            isLoginInProgress = false,
                        )
                    }
                    saveUser(authorizedName)
                }
        }
    }

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.UpdateName -> updateName(action.value)
            OnboardingAction.ShowLoginSheet -> showLoginSheet()
            OnboardingAction.HideLoginSheet -> hideLoginSheet()
            is OnboardingAction.LogIn -> logIn(
                email = action.email,
                password = action.password,
            )
            OnboardingAction.SaveUser -> saveUser(state.value.name.trim())
        }
    }

    private fun updateName(value: String) {
        _state.update {
            it.copy(name = value)
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
                loginError = null,
            )
        }
    }

    private fun logIn(
        email: String,
        password: String,
    ) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank() || state.value.isLoginInProgress) return

        viewModelScope.launch {
            _state.update {
                it.copy(isLoginInProgress = true, loginError = null)
            }

            try {
                accountService.logIn(
                    email = trimmedEmail,
                    password = password,
                )
                _state.update {
                    it.copy(
                        isLoginSheetVisible = false,
                        isFinalizingInProgress = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loginError = e.message)
                }
            } finally {
                _state.update {
                    it.copy(isLoginInProgress = false)
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun saveUser(name: String) {
        if (name.isBlank() || state.value.isSaving) return

        viewModelScope.launch {
            _state.update {
                it.copy(isSaving = true)
            }

            saveUserUseCase(
                name = name,
                kind = UserKind.MainUser,
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
