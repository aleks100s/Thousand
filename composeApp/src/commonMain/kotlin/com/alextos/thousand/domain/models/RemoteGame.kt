package com.alextos.thousand.domain.models

data class RemoteGame(
    val id: Long = 0,
    val settings: GameSettings = GameSettings(),
    val players: List<Player>,
    val host: String,
    val key: String,
    val currentPlayer: Player?,
    val currentTurn: List<DiceRoll> = emptyList(),
    val currentRoll: DiceRoll? = null,
) {
    fun isFinished(): Boolean = players.any { it.isWinner }
}
