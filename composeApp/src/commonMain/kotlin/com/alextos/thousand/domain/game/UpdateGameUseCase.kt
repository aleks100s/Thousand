package com.alextos.thousand.domain.game

import com.alextos.thousand.domain.GameConstants
import com.alextos.thousand.domain.game.server.GameStatus
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.repository.GameRepository
import kotlin.time.Clock

class UpdateGameUseCase(
    private val repository: GameRepository
) {
    suspend operator fun invoke(
        game: Game,
        currentTurn: Turn
    ): GameStatus {
        var status = GameStatus.ONGOING
        game.players.forEach { player ->
            val turnResult = currentTurn.results.firstOrNull { it.player == player }
            if (turnResult != null) {
                player.isWinner = turnResult.newScore >= GameConstants.GAME_GOAL
                status = if (player.isWinner) {
                    GameStatus.FINISHED
                } else {
                    GameStatus.ONGOING
                }
            }
        }
        game.finishedAt = if (status == GameStatus.FINISHED) Clock.System.now() else null
        repository.saveGame(game)
        return status
    }
}