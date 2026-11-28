package com.alextos.thousand.domain.models

import kotlin.time.Instant

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
    val onlinePlayerIds: Set<String> = emptySet(),
    val finishedAt: Instant? = null,
    val achievementTurn: RemoteAchievementTurn? = null,
) {
    fun startAchievementRound(): RemoteGame = copy(achievementTurn = RemoteAchievementTurn.newRound())

    fun withCompletedAchievementTurn(turn: Turn, nextPlayerIndex: Int): RemoteGame {
        val previous = achievementTurn
        return copy(
            achievementTurn = RemoteAchievementTurn(
                roundId = previous?.roundId ?: RemoteAchievementTurn.newRound().roundId,
                number = (previous?.number ?: 0) + 1,
                nextPlayerIndex = nextPlayerIndex,
                hasCompleteNumbering = previous?.hasCompleteNumbering == true &&
                    players[previous.number % players.size].user.id == turn.player.user.id,
                playerId = turn.player.user.id,
                total = turn.total,
                pitPlayerIds = turn.effects.filter { it.effect == Effect.PIT_FALL }
                    .map { it.affectedPlayer.user.id }.toSet(),
            ),
        )
    }

    fun isFinished(): Boolean = players.any { it.isWinner }
    fun currentPlayer() = players.getOrNull(currentPlayerIndex)
}
