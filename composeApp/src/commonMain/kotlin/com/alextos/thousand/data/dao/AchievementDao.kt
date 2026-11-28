package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.alextos.thousand.data.models.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query(
        """
        SELECT * FROM unlocked_achievements
        WHERE userId = (SELECT id FROM users WHERE kind = 2 LIMIT 1)
        """,
    )
    fun observeUnlockedAchievements(): Flow<List<AchievementEntity>>

    @Query(
        """
        INSERT OR IGNORE INTO unlocked_achievements (userId, achievementId, unlockedAt)
        SELECT :userId, :achievementId, :unlockedAt
        WHERE :userId = (SELECT id FROM users WHERE kind = 2 LIMIT 1)
        """,
    )
    suspend fun insertForMainUser(userId: String, achievementId: String, unlockedAt: Long): Long
}
