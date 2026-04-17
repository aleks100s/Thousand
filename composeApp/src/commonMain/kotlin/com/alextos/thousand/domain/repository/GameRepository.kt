package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.User
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getAllGames(): Flow<List<Game>>

    fun getAllUsers(): Flow<List<User>>

    suspend fun saveUser(user: User)

    suspend fun saveGame(game: Game): Long

    suspend fun getGame(id: Long): Game?

    suspend fun getAllTurns(gameID: Long): List<Turn>

    suspend fun saveTurn(turn: Turn, game: Game): Long
}
