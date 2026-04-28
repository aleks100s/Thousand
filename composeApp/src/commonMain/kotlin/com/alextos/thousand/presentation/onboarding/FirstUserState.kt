package com.alextos.thousand.presentation.onboarding

import com.alextos.thousand.domain.models.User

data class FirstUserState(
    val isLoading: Boolean = true,
    val isFirstUserRequired: Boolean = false,
    val localUsers: List<User> = emptyList(),
    val name: String = "",
    val isSaving: Boolean = false,
)
