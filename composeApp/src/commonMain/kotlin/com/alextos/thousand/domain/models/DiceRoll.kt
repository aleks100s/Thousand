package com.alextos.thousand.domain.models

data class DiceRoll(
    val id: Long,
    val order: Int,
    val dice: List<Die>,
    val result: Int
)
