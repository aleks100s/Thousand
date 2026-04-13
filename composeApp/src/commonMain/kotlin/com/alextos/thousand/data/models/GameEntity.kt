package com.alextos.thousand.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: Int,
    val startedAt: Long,
    val finishedAt: Long?,
)
