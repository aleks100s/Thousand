package com.alextos.thousand.domain.repository

import com.alextos.thousand.presentation.game.components.GameSettings

interface MultiplayerRepository {
    suspend fun createLobby(gameSettings: GameSettings): String
}
