package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.Lobby
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.FinishedGameSaveResult
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserReaction
import kotlinx.coroutines.flow.Flow

class LobbyNotFoundException : IllegalStateException("Лобби больше недоступно.")

interface MultiplayerRepository {
    suspend fun createLobby(gameSettings: GameSettings, player: User): String
    suspend fun joinLobby(id: String, player: User): String
    fun connectToLobby(key: String): Flow<Lobby>
    suspend fun disconnectFromLobby(key: String)
    suspend fun startGame(key: String)
    fun observeGame(key: String): Flow<RemoteGame>
    suspend fun updateGame(game: RemoteGame)
    suspend fun sendReaction(gameKey: String, reaction: UserReaction)
    suspend fun updatePlayerOnlineStatus(gameKey: String, isOnline: Boolean)
    suspend fun saveGameResult(game: RemoteGame): FinishedGameSaveResult?
    suspend fun deleteGame(key: String)
    fun userLobbies(): Flow<List<Lobby>>
    fun userGames(): Flow<List<RemoteGame>>
}
