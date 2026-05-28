package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.Lobby
import kotlinx.coroutines.flow.Flow

interface MultiplayerManager {
    suspend fun createLobby(gameSettings: GameSettings): String
    suspend fun joinLobby(id: String): String
    fun connectToLobby(key: String): Flow<Lobby>
    suspend fun disconnectFromLobby(key: String)
    suspend fun startGame(key: String)
    fun observeGame(key: String): Flow<Game>
    suspend fun updateGame(game: Game)
    suspend fun deleteGame(key: String)
    fun userLobbies(): Flow<List<Lobby>>
    fun userGames(): Flow<List<Game>>
}
