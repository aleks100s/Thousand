package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.alextos.thousand.data.models.GameEntity

@Dao
interface GameDao {
    @Upsert
    suspend fun insert(game: GameEntity)
}
