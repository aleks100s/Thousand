package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.alextos.thousand.data.models.TurnEffectEntity

@Dao
interface TurnEffectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(effects: List<TurnEffectEntity>)
}
