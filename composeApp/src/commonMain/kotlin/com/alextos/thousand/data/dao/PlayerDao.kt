package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.alextos.thousand.data.models.PlayerEntity
import com.alextos.thousand.data.models.combined.PlayerWithUser
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Transaction
    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<PlayerWithUser>>

    @Upsert
    suspend fun upsert(players: List<PlayerEntity>)
}
