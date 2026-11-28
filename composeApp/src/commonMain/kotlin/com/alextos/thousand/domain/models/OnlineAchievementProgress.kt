package com.alextos.thousand.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class OnlineAchievementProgress(
    val observedTurns: Set<Int> = emptySet(),
    val personalTurns: Int = 0,
    val pitFalls: Int = 0,
    val hadZeroTurn: Boolean = false,
)
