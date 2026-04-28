package com.alextos.thousand.domain.models

data class User(
    val id: Long = 0,
    val name: String,
    val kind: UserKind = UserKind.LocalUser,
)
