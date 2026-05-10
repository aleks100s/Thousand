package com.alextos.thousand.domain.models

data class TurnEffect(
    val id: Long = 0,
    val affectedPlayer: Player,
    val effect: Effect
)
