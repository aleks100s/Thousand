package com.alextos.thousand.application

data class AppState(
    val isLoading: Boolean = true,
    val isOnboardingRequired: Boolean = false,
    val hideMultiplayer: Boolean = false,
)
