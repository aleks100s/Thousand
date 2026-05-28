package com.alextos.thousand.domain.models

data class Lobby(
    val id: String = "",
    val key: String,
    val settings: GameSettings = GameSettings(),
    var players: List<User> = emptyList(),
    val host: String = "",
    val game: String = ""
)
