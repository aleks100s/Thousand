package com.alextos.thousand.domain.models

data class DiceRoll(
    val id: Int,
    val playerID: Int,
    val turnID: Int,
    val order: Int,
    val dice: List<DieResult>,
    val total: Int
)
