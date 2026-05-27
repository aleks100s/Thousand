package com.alextos.thousand.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val startedAt: Long,
    val finishedAt: Long?,
)
