package com.alextos.thousand.data.repository

import com.alextos.thousand.data.dao.AchievementDao
import com.alextos.thousand.data.local.KeyValueStorage
import com.alextos.thousand.domain.models.AchievementId
import com.alextos.thousand.domain.models.OnlineAchievementProgress
import com.alextos.thousand.domain.models.UnlockedAchievement
import com.alextos.thousand.domain.repository.AchievementStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

class RoomAchievementStore(
    private val achievementDao: AchievementDao,
    private val storage: KeyValueStorage,
) : AchievementStore {
    override fun observeUnlockedAchievements(): Flow<List<UnlockedAchievement>> =
        achievementDao.observeUnlockedAchievements().map { achievements ->
            achievements.mapNotNull { entity ->
                AchievementId.fromStorageId(entity.achievementId)?.let { id ->
                    UnlockedAchievement(id, Instant.fromEpochMilliseconds(entity.unlockedAt))
                }
            }
        }

    override suspend fun getOnlineProgress(userId: String, roundId: String): OnlineAchievementProgress =
        storage.getString(progressKey(userId, roundId)).first()?.let { Json.decodeFromString(it) }
            ?: OnlineAchievementProgress()

    override suspend fun saveOnlineProgress(userId: String, roundId: String, progress: OnlineAchievementProgress) {
        storage.saveString(progressKey(userId, roundId), Json.encodeToString(progress))
    }

    private fun progressKey(userId: String, roundId: String): String =
        "online_achievement_progress:$userId:$roundId"

    override suspend fun unlockForMainUser(userId: String, achievementId: AchievementId): Boolean =
        achievementDao.insertForMainUser(
            userId = userId,
            achievementId = achievementId.storageId,
            unlockedAt = Clock.System.now().toEpochMilliseconds(),
        ) != -1L
}
