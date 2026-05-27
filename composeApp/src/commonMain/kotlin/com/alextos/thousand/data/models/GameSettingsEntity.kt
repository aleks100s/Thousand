package com.alextos.thousand.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_settings",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
)
data class GameSettingsEntity(
    @PrimaryKey val gameId: Long,
    @ColumnInfo(defaultValue = "1")
    val isNotificationEnabled: Boolean = true,
    @ColumnInfo(defaultValue = "1")
    val isVirtualDiceEnabled: Boolean = true,
    @ColumnInfo(defaultValue = "1")
    val isShakeEnabled: Boolean = true,
    @ColumnInfo(defaultValue = "1")
    val hasStartLimit: Boolean = true,
    @ColumnInfo(defaultValue = "1")
    val isBarrel1Active: Boolean = true,
    @ColumnInfo(defaultValue = "1")
    val isBarrel2Active: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val isBarrel3Active: Boolean = false,
    @ColumnInfo(defaultValue = "1")
    val isTripleBoltFineActive: Boolean = true,
    @ColumnInfo(defaultValue = "1")
    val isOvertakeFineActive: Boolean = true,
)
