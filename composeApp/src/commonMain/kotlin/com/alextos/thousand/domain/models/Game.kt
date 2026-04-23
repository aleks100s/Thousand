package com.alextos.thousand.domain.models

import kotlin.time.Clock
import kotlin.time.Instant

data class Game(
    val id: Long = 0,
    val startedAt: Instant = Clock.System.now(),
    var finishedAt: Instant? = null,
    val isShakeEnabled: Boolean = true,
    val isVirtualDiceEnabled: Boolean = true,
    val isNotificationEnabled: Boolean = true,
    val players: List<Player>
) {
    fun isFinished(): Boolean = finishedAt != null
}
