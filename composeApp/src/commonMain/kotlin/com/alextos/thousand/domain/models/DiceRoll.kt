package com.alextos.thousand.domain.models

data class DiceRoll(
    val id: Long = 0,
    val order: Int,
    val dice: List<Die>,
    val result: Int
)
