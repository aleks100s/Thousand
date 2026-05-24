package com.alextos.thousand.domain.models

data class Lobby(
    var settings: GameSettings = GameSettings(),
    var players: List<Player> = emptyList(),
    var host: String = "",
    var id: String = ""
) {
    data class Player(
        var id: String = "",
        var name: String = ""
    )
}
