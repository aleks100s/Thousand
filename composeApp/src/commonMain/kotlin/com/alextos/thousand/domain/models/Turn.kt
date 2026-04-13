package com.alextos.thousand.domain.models

data class Turn(
    val id: Int,
    val playerID: Int,
    val gameID: Int,
    val order: Int,
    val rolls: List<DiceRoll>,
    val total: Int,
    val effects: List<Effect>
)
