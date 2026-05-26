package com.alextos.thousand.domain.models

data class Lobby(
    var settings: GameSettings = GameSettings(),
    var players: List<UserProfile> = emptyList(),
    var host: String = "",
    var id: String = "",
    var game: String = ""
)
