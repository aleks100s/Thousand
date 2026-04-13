package com.alextos.thousand.domain.models

data class Player(
    val id: Int,
    val user: User,
    val currentScore: Int,
    val isWinner: Boolean
)
