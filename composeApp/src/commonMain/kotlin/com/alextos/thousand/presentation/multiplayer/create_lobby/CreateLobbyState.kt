package com.alextos.thousand.presentation.multiplayer.create_lobby

import com.alextos.thousand.domain.models.GameSettings

data class CreateLobbyState(
    val gameSettings: GameSettings = GameSettings(),
    val isLoading: Boolean = false
)
