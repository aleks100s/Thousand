package com.alextos.thousand.domain.models

data class RemoteGame(
    val id: Long = 0,
    val settings: GameSettings = GameSettings(),
    val players: List<Player>,
    val host: String = "",
    val key: String = "",
) {
    fun isFinished(): Boolean = players.any { it.isWinner }
}