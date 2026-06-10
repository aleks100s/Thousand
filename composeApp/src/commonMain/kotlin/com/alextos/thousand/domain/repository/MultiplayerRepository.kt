package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.Lobby
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.RemoteUserInfo
import kotlinx.coroutines.flow.Flow

interface MultiplayerRepository {
    suspend fun createLobby(gameSettings: GameSettings): String
    suspend fun joinLobby(id: String): String
    fun connectToLobby(key: String): Flow<Lobby>
    suspend fun disconnectFromLobby(key: String)
    suspend fun startGame(key: String)
    fun observeGame(key: String): Flow<RemoteGame>
    suspend fun updateGame(game: RemoteGame)
    suspend fun finishGame(game: RemoteGame)
    suspend fun deleteGame(key: String)
    suspend fun userInfo(userId: String): RemoteUserInfo?
    fun userLobbies(): Flow<List<Lobby>>
    fun userGames(): Flow<List<RemoteGame>>
}
