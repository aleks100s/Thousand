package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.alextos.thousand.data.models.PlayerEntity

@Dao
interface PlayerDao {
    @Upsert
    suspend fun insert(players: List<PlayerEntity>)
}
