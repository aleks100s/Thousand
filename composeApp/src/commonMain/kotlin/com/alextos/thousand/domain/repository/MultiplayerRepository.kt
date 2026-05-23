package com.alextos.thousand.domain.repository

import com.alextos.thousand.presentation.game.components.GameSettings

interface MultiplayerRepository {
    fun createLobby(gameSettings: GameSettings, host: String): String
}
