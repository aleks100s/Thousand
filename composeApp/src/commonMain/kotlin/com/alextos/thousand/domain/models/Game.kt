package com.alextos.thousand.domain.models

import kotlin.time.Instant

data class Game(
    val id: Long,
    val startedAt: Instant,
    val finishedAt: Instant?,
    val players: List<Player>
)
