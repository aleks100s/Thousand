package com.alextos.thousand.presentation.onboarding

sealed interface OnboardingAction {
    data class UpdateName(val value: String) : OnboardingAction
    data object ShowLoginSheet : OnboardingAction
    data object HideLoginSheet : OnboardingAction
    data class LogIn(
        val email: String,
        val password: String,
    ) : OnboardingAction
    data object SaveUser : OnboardingAction
}
