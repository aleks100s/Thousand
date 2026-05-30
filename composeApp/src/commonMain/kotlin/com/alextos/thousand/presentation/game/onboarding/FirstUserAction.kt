package com.alextos.thousand.presentation.game.onboarding

sealed interface FirstUserAction {
    data class UpdateName(val value: String) : FirstUserAction
    data object SaveUser : FirstUserAction
}
