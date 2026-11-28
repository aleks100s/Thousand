package com.alextos.thousand.data.models

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "unlocked_achievements",
    primaryKeys = ["userId", "achievementId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
)
data class AchievementEntity(
    val userId: String,
    val achievementId: String,
    val unlockedAt: Long,
)
