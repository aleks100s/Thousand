package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.AchievementId
import com.alextos.thousand.domain.models.UnlockedAchievement
import kotlinx.coroutines.flow.Flow

interface AchievementStore {
    fun observeUnlockedAchievements(): Flow<List<UnlockedAchievement>>

    suspend fun unlockForMainUser(userId: String, achievementId: AchievementId): Boolean
}
