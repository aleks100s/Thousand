package com.alextos.thousand.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val kind: Int,
    @androidx.room.ColumnInfo(defaultValue = "''") val color: String = "",
    @androidx.room.ColumnInfo(defaultValue = "'Neutral'") val avatar: String = "Neutral",
)
