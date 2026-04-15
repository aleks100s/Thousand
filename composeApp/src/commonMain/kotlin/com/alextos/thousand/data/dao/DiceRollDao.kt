package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Upsert
import com.alextos.thousand.data.models.DiceRollEntity

@Dao
interface DiceRollDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(roll: DiceRollEntity): Long
}
