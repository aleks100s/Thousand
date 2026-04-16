package com.alextos.thousand.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "turn_effects",
    foreignKeys = [
        ForeignKey(
            entity = TurnEntity::class,
            parentColumns = ["id"],
            childColumns = ["turnId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["affectedPlayerId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["turnId"]),
        Index(value = ["affectedPlayerId"]),
    ],
)
data class TurnEffectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val turnId: Long,
    val order: Int,
    val effectType: String,
    val affectedPlayerId: Long
)
