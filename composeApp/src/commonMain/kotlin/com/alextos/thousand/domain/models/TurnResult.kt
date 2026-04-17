package com.alextos.thousand.domain.models

data class TurnResult(
    val id: Int = 0,
    val player: Player,
    val scoreChange: Int,
    val newScore: Int
)
