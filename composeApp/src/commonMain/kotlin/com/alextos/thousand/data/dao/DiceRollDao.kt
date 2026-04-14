package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.alextos.thousand.data.models.DiceRollEntity

@Dao
interface DiceRollDao {
    @Upsert
    suspend fun insert(roll: DiceRollEntity)
}
