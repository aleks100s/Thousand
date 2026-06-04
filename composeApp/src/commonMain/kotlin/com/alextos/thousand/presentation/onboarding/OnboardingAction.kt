package com.alextos.thousand.presentation.onboarding

sealed interface OnboardingAction {
    data class UpdateName(val value: String) : OnboardingAction
    data object SaveUser : OnboardingAction
}
