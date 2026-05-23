package com.alextos.thousand.data.repository

import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.presentation.game.components.GameSettings

class MultiplayerRepositoryImpl : MultiplayerRepository {
    override fun createLobby(gameSettings: GameSettings, host: String): String {
        return ""
    }
}
