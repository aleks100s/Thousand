package com.alextos.thousand.domain.usecase.game

import com.alextos.thousand.domain.models.FinishedGameSaveResult
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.RemoteUserInfo
import com.alextos.thousand.domain.repository.MultiplayerRepository
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

class FinishRemoteGameUseCase(
    private val multiplayerRepository: MultiplayerRepository
) {
    suspend operator fun invoke(
        game: RemoteGame,
        userInfo: Map<String, RemoteUserInfo>,
        onGamePersisted: suspend (FinishedGameSaveResult) -> Unit = {},
    ) {
        if (!game.isFinished()) return
        val savedResult = multiplayerRepository.saveGameResult(game) ?: return
        onGamePersisted(savedResult)
        if (!savedResult.newlyFinished) return
        val finishedGame = savedResult.game
        val winner = finishedGame.players.firstOrNull { it.isWinner } ?: return

        val updatedUserInfo = userInfo.toMutableMap()
        val losers = finishedGame.players.filter { it != winner }

        if (losers.isEmpty()) {
            multiplayerRepository.saveGameStatistics(finishedGame, updatedUserInfo)
            return
        }

        val effectiveK = K_FACTOR / losers.size
        val originalRatings = finishedGame.players.associate { player ->
            player.user.id to (userInfo[player.user.id]?.rating ?: INITIAL_RATING)
        }
        val ratingDeltas = mutableMapOf<String, Float>()
        val winnerId = winner.user.id
        val winnerRating = originalRatings[winnerId] ?: INITIAL_RATING

        losers.forEach { loser ->
            val loserId = loser.user.id
            val loserRating = originalRatings[loserId] ?: INITIAL_RATING
            val winnerExpected = calculateExpectedScore(winnerRating, loserRating)
            val loserExpected = calculateExpectedScore(loserRating, winnerRating)

            ratingDeltas[winnerId] = (ratingDeltas[winnerId] ?: 0f) + effectiveK * (1f - winnerExpected)
            ratingDeltas[loserId] = (ratingDeltas[loserId] ?: 0f) + effectiveK * (0f - loserExpected)
        }

        ratingDeltas.forEach { (userId, delta) ->
            val oldRating = originalRatings[userId] ?: INITIAL_RATING
            val newRating = oldRating + delta.roundToInt()
            updatedUserInfo[userId]?.rating = max(newRating, INITIAL_RATING)
        }

        multiplayerRepository.saveGameStatistics(finishedGame, updatedUserInfo)
    }

    private fun calculateExpectedScore(
        playerRating: Int,
        opponentRating: Int
    ): Float {
        return 1f / (1f + 10f.pow((opponentRating - playerRating) / 400f))
    }

    private companion object {
        const val INITIAL_RATING = 0
        const val K_FACTOR = 16f
    }
}
