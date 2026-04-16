package com.alextos.thousand.domain.models

data class TurnEffect(
    val id: Long,
    val affectedPlayer: Player,
    val effect: Effect
)
