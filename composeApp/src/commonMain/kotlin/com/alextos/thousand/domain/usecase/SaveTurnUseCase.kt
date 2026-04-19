package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.TurnResult
import com.alextos.thousand.domain.repository.GameRepository

class SaveTurnUseCase(
    private val repository: GameRepository
) {
    suspend operator fun invoke(
        currentPlayer: Player,
        rolls: List<DiceRoll>,
        game: Game
    ): Turn {
        val total = calculateTurnResult(rolls)
        val results = calculateResults(game, currentPlayer, total)
        val turn = Turn(
            player = currentPlayer,
            rolls = rolls,
            total = total,
            effects = emptyList(),
            results = results
        )
        val turnId = repository.saveTurn(turn, game)
        return turn.copy(id = turnId)
    }

    private fun calculateTurnResult(
        rolls: List<DiceRoll>
    ): Int {
        if (rolls.lastOrNull()?.result == 0) {
            return 0
        }
        return rolls.sumOf { it.result }
    }

    private fun calculateResults(
        game: Game,
        currentPlayer: Player,
        total: Int
    ): List<TurnResult> {
        return game.players.mapNotNull { player ->
            if (player == currentPlayer) {
                TurnResult(
                    player = currentPlayer,
                    scoreChange = total,
                    newScore = currentPlayer.currentScore + total
                )
            } else {
                null
            }
        }
    }
}