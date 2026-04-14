package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.alextos.thousand.data.models.TurnEntity
import com.alextos.thousand.data.models.combined.TurnWithRelations

@Dao
interface TurnDao {
    @Upsert
    suspend fun insert(turn: TurnEntity)

    @Query("SELECT * FROM turns WHERE gameId = :gameID")
    suspend fun getTurns(gameID: Int): List<TurnWithRelations>
}
