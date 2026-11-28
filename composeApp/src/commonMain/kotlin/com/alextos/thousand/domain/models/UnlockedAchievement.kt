package com.alextos.thousand.domain.models

import kotlin.time.Instant

data class UnlockedAchievement(
    val id: AchievementId,
    val unlockedAt: Instant,
)
