package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Upsert
import com.alextos.thousand.data.models.DieEntity

@Dao
interface DieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dice: List<DieEntity>)
}
