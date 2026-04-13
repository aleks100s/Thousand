package com.alextos.thousand.domain.models

data class TurnEffect(
    val id: Int,
    val turnID: Int,
    val affectedPlayerID: Int,
    val effect: Effect,
    val penaltyValue: Int
)