package com.alextos.thousand.presentation.onboarding

data class OnboardingState(
    val name: String = "",
    val isSaving: Boolean = false,
    val isLoginSheetVisible: Boolean = false,
    val isLoginInProgress: Boolean = false,
    val loginError: String? = null,
    val isFinalizingInProgress: Boolean = false
)
