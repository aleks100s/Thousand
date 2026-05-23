package com.alextos.thousand.domain.models

data class Lobby(
    val settings: GameSettings,
    val isCurrentPlayerHost: Boolean
)
