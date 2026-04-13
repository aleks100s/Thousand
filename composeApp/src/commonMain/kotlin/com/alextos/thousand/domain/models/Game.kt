package com.alextos.thousand.domain.models

data class Game(
    val id: Int,
    val startedAt: Long,
    val finishedAt: Long?,
    val players: List<Player>,
    val turns: List<Turn>
)
