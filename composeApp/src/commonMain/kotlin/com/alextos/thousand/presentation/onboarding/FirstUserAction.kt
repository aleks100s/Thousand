package com.alextos.thousand.presentation.onboarding

import com.alextos.thousand.domain.models.User

sealed interface FirstUserAction {
    data class UpdateName(val value: String) : FirstUserAction
    data class SelectExistingUser(val user: User) : FirstUserAction
    data object SaveUser : FirstUserAction
}
