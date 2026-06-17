package com.alextos.thousand.domain.models

data class RemoteGame(
    val id: Long = 0,
    val settings: GameSettings = GameSettings(),
    val players: List<Player>,
    val host: String,
    val key: String,
    val currentPlayerIndex: Int = 0,
    val currentTurn: List<DiceRoll> = emptyList(),
    val currentRoll: DiceRoll? = null,
    val rollAbility: RollAbility = RollAbility.REQUIRED,
    val buttons: List<GameButton> = emptyList(),
    val messagesToShow: List<String> = emptyList(),
    val reaction: UserReaction? = null,
) {
    fun isFinished(): Boolean = players.any { it.isWinner }
    fun currentPlayer() = players.getOrNull(currentPlayerIndex)
}
