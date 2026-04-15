package com.alextos.thousand.domain.models

data class Turn(
    val id: Long,
    val order: Int,
    val user: User,
    val rolls: List<DiceRoll>,
    val total: Int,
    val effects: List<Effect>
)
