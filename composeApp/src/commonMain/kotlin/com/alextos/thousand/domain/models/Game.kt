package com.alextos.thousand.domain.models

import kotlin.time.Clock
import kotlin.time.Instant

data class Game(
    val id: Long = 0,
    val startedAt: Instant = Clock.System.now(),
    var finishedAt: Instant? = null,
    val settings: GameSettings = GameSettings(),
    val players: List<Player>,
    // multiplayer only
    val host: String = "",
    val key: String = "",
) {
    fun isFinished(): Boolean = finishedAt != null
}
