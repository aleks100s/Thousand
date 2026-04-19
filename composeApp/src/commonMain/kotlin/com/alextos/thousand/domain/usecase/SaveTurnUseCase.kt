package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.GameStatus
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.TurnResult
import com.alextos.thousand.domain.repository.GameRepository
import kotlin.time.Clock

class SaveTurnUseCase(
    private val repository: GameRepository
) {
    companion object {
        const val GAME_GOAL = 100
    }

    suspend operator fun invoke(
        player: Player,
        rolls: List<DiceRoll>,
        game: Game
    ): Pair<GameStatus, Turn> {
        val total = calculateTurnResult(rolls)
        val turnResult = TurnResult(
            player = player,
            scoreChange = total,
            newScore = player.currentScore + total
        )
        val turn = Turn(
            player = player,
            rolls = rolls,
            total = total,
            effects = emptyList(),
            results = listOf(turnResult)
        )
        player.currentScore = turnResult.newScore
        player.isWinner = turnResult.newScore >= GAME_GOAL
        val status = if (player.isWinner) {
            GameStatus.FINISHED
        } else {
            GameStatus.ONGOING
        }
        game.finishedAt = if (status == GameStatus.FINISHED) Clock.System.now() else null
        val turnId = repository.saveTurn(turn, game)
        repository.saveGame(game)
        return status to turn.copy(id = turnId)
    }

    private fun calculateTurnResult(
        rolls: List<DiceRoll>
    ): Int {
        if (rolls.lastOrNull()?.result == 0) {
            return 0
        }
        return rolls.sumOf { it.result }
    }
}