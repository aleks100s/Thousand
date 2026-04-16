package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.alextos.thousand.data.models.TurnEntity
import com.alextos.thousand.data.models.combined.TurnWithRelations

@Dao
interface TurnDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(turn: TurnEntity): Long

    @Transaction
    @Query("SELECT * FROM turns WHERE gameId = :gameID")
    suspend fun getTurns(gameID: Long): List<TurnWithRelations>
}
