package com.alextos.thousand.domain.models

data class Lobby(
    var id: String = "",
    var settings: GameSettings = GameSettings(),
    var players: List<User> = emptyList(),
    var host: String = "",
    var game: String = ""
)
