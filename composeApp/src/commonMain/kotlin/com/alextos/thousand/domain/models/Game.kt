package com.alextos.thousand.domain.models

import kotlin.time.Instant

data class Game(
    val id: Int,
    val startedAt: Instant,
    val finishedAt: Instant?,
    val players: List<Player>,
    val turns: List<Turn>
)
