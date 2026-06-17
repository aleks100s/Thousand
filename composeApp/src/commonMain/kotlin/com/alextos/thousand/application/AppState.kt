package com.alextos.thousand.application

data class AppState(
    val isLoading: Boolean = true,
    val isLaunchFinished: Boolean = false,
    val isOnboardingRequired: Boolean = false,
    val hideMultiplayer: Boolean = false,
)
