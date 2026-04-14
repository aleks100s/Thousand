package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.alextos.thousand.data.models.TurnEffectEntity

@Dao
interface TurnEffectDao {
    @Upsert
    suspend fun insert(effect: TurnEffectEntity)
}
