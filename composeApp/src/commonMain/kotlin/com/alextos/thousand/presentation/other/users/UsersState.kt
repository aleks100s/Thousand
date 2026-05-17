package com.alextos.thousand.presentation.other.users

import com.alextos.thousand.domain.models.User

data class UsersState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
)
