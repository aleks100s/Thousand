package com.alextos.thousand.domain.models

data class TurnEffect(
    val id: Int,
    val affectedPlayer: Player,
    val effect: Effect,
    val penaltyValue: Int
)
