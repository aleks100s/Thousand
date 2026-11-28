package com.alextos.thousand.data.repository

import com.alextos.thousand.data.dao.AchievementDao
import com.alextos.thousand.domain.models.AchievementId
import com.alextos.thousand.domain.models.UnlockedAchievement
import com.alextos.thousand.domain.repository.AchievementStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Instant

class RoomAchievementStore(
    private val achievementDao: AchievementDao,
) : AchievementStore {
    override fun observeUnlockedAchievements(): Flow<List<UnlockedAchievement>> =
        achievementDao.observeUnlockedAchievements().map { achievements ->
            achievements.mapNotNull { entity ->
                AchievementId.fromStorageId(entity.achievementId)?.let { id ->
                    UnlockedAchievement(id, Instant.fromEpochMilliseconds(entity.unlockedAt))
                }
            }
        }

    override suspend fun unlockForMainUser(userId: String, achievementId: AchievementId): Boolean =
        achievementDao.insertForMainUser(
            userId = userId,
            achievementId = achievementId.storageId,
            unlockedAt = Clock.System.now().toEpochMilliseconds(),
        ) != -1L
}
