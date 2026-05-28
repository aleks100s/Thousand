package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.Lobby
import kotlinx.coroutines.flow.Flow

interface MultiplayerManager {
    suspend fun createLobby(gameSettings: GameSettings): String
    suspend fun joinLobby(id: String)
    fun connectToLobby(id: String): Flow<Lobby>
    suspend fun disconnectFromLobby(id: String)
    suspend fun startGame(id: String)
    fun observeGame(id: String): Flow<Game>
    suspend fun updateGame(id: String, game: Game)
    fun userLobbies(): Flow<List<Lobby>>
    fun userGames(): Flow<List<Game>>
}
