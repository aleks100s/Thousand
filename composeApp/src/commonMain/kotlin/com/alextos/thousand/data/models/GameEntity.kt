package com.alextos.thousand.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val startedAt: Long,
    val finishedAt: Long?,
    @ColumnInfo(defaultValue = "1")
    val isShakeEnabled: Boolean = true,
    @ColumnInfo(defaultValue = "1")
    val isVirtualDiceEnabled: Boolean = true,
    @ColumnInfo(defaultValue = "1")
    val isNotificationEnabled: Boolean = true,
)
