package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.alextos.thousand.data.models.TurnEntity

@Dao
interface TurnDao {
    @Upsert
    suspend fun insert(turn: TurnEntity)
}
