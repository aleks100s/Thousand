package com.alextos.thousand.domain.models

data class Lobby(
    var settings: GameSettings = GameSettings(),
    var players: List<User> = emptyList(),
    var host: String = "",
    var id: String = "",
    var game: String = ""
)
