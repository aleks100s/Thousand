package com.alextos.thousand.domain.models

import kotlin.time.Instant

data class Game(
    val id: Long = 0,
    val startedAt: Instant,
    val finishedAt: Instant?,
    val players: List<Player>
) {
    fun isFinished(): Boolean = finishedAt != null
}
