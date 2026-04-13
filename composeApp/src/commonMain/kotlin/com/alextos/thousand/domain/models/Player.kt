package com.alextos.thousand.domain.models

data class Player(
    val id: Int,
    val userID: Int,
    val gameID: Int,
    val currentScore: Int
)
