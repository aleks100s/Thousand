package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.alextos.thousand.data.models.DieEntity

@Dao
interface DieDao {
    @Upsert
    suspend fun insert(die: DieEntity)
}
