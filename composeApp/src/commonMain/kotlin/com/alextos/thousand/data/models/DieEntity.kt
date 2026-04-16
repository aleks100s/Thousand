package com.alextos.thousand.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dice",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DiceRollEntity::class,
            parentColumns = ["id"],
            childColumns = ["rollId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["playerId"]),
        Index(value = ["rollId"]),
    ],
)
data class DieEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val playerId: Long,
    val rollId: Long,
    val order: Int,
    val value: Int,
)
