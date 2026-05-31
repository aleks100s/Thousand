package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.User
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getAllGames(): Flow<List<Game>>

    fun getAllUsers(): Flow<List<User>>

    suspend fun getMainUser(): User?

    suspend fun saveUser(user: User)

    suspend fun deleteUser(userId: String)

    suspend fun createGame(game: Game): Game
    suspend fun saveGame(game: Game)

    suspend fun getGame(id: Long): Game?

    suspend fun getAllTurns(gameID: Long): List<Turn>

    fun getAllTurns(): Flow<List<Turn>>

    fun getTurnEffectCount(userId: String, effect: Effect): Flow<Int>

    suspend fun saveTurn(turn: Turn, game: Game): Turn

    suspend fun deleteGame(gameId: Long)
}
