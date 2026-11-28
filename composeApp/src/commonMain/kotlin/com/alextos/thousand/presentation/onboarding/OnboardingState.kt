package com.alextos.thousand.presentation.onboarding

import com.alextos.thousand.domain.models.PlayerAvatar

data class OnboardingState(
    val name: String = "",
    val avatar: PlayerAvatar = PlayerAvatar.Neutral,
    val isSaving: Boolean = false,
)
