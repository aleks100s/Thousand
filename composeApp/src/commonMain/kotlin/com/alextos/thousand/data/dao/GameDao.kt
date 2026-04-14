package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.alextos.thousand.data.models.GameEntity
import com.alextos.thousand.data.models.combined.GameWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Upsert
    suspend fun insert(game: GameEntity)

    @Query("SELECT * FROM games")
    fun getAllGames(): Flow<List<GameWithRelations>>
}
