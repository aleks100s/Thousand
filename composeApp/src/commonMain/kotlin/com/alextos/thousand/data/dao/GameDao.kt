package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.alextos.thousand.data.models.GameEntity
import com.alextos.thousand.data.models.combined.GameWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Transaction
    @Query("SELECT * FROM games")
    fun getAllGames(): Flow<List<GameWithRelations>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: GameEntity): Long

    @Transaction
    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getGame(id: Long): GameWithRelations?

    @Update
    suspend fun upsert(game: GameEntity)

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun delete(gameId: Long)
}
