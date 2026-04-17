package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Upsert
import com.alextos.thousand.data.models.TurnResultEntity

@Dao
interface TurnResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(results: List<TurnResultEntity>)
}
