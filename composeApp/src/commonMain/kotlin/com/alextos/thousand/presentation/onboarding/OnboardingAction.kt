package com.alextos.thousand.presentation.onboarding

import com.alextos.thousand.domain.models.PlayerAvatar

sealed interface OnboardingAction {
    data class UpdateName(val value: String) : OnboardingAction
    data class SelectAvatar(val avatar: PlayerAvatar) : OnboardingAction
    data object SaveUser : OnboardingAction
}
