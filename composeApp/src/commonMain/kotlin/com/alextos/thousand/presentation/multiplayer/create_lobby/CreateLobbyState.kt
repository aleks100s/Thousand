package com.alextos.thousand.presentation.multiplayer.create_lobby

import com.alextos.thousand.presentation.game.components.GameSettings

data class CreateLobbyState(
    val gameSettings: GameSettings = GameSettings(),
)
