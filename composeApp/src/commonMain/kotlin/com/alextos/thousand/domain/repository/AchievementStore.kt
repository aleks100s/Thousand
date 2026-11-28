package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.AchievementId
import com.alextos.thousand.domain.models.OnlineAchievementProgress
import com.alextos.thousand.domain.models.UnlockedAchievement
import kotlinx.coroutines.flow.Flow

interface AchievementStore {
    fun observeUnlockedAchievements(): Flow<List<UnlockedAchievement>>

    suspend fun getOnlineProgress(userId: String, roundId: String): OnlineAchievementProgress

    suspend fun saveOnlineProgress(userId: String, roundId: String, progress: OnlineAchievementProgress)

    suspend fun unlockForMainUser(userId: String, achievementId: AchievementId): Boolean
}
