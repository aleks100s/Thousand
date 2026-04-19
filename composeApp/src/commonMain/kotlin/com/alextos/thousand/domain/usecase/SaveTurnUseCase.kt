package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.TurnEffect
import com.alextos.thousand.domain.models.TurnResult
import com.alextos.thousand.domain.repository.GameRepository

class SaveTurnUseCase(
    private val repository: GameRepository
) {
    companion object {
        private const val PIT_SCORE = 555
    }

    suspend operator fun invoke(
        currentPlayer: Player,
        rolls: List<DiceRoll>,
        game: Game
    ): Turn {
        val turnTotal = calculateTurnResult(rolls)
        currentPlayer.currentScore += turnTotal
        val pair = checkEffects(currentPlayer)
        if (pair != null) {
            val turn = Turn(
                player = currentPlayer,
                rolls = rolls,
                total = turnTotal,
                effects = listOf(pair.first),
                results = listOf(pair.second)
            )
            return repository.saveTurn(turn, game)
        }

        val results = calculateResults(game, currentPlayer, turnTotal)
        val turn = Turn(
            player = currentPlayer,
            rolls = rolls,
            total = turnTotal,
            effects = emptyList(),
            results = results
        )
        return repository.saveTurn(turn, game)
    }

    private fun calculateTurnResult(
        rolls: List<DiceRoll>
    ): Int {
        if (rolls.lastOrNull()?.result == 0) {
            return 0
        }
        return rolls.sumOf { it.result }
    }

    private fun checkEffects(
        player: Player
    ): Pair<TurnEffect, TurnResult>? {
        if (player.currentScore == PIT_SCORE) {
            player.currentScore = 0
            val effect = TurnEffect(affectedPlayer = player, effect = Effect.PIT_FALL)
            val result = TurnResult(player = player, scoreChange = -PIT_SCORE, newScore = 0)
            return effect to result
        }
        return null
    }

    private fun calculateResults(
        game: Game,
        currentPlayer: Player,
        turnTotal: Int
    ): List<TurnResult> {
        return game.players.mapNotNull { player ->
            if (player == currentPlayer) {
                TurnResult(
                    player = currentPlayer,
                    scoreChange = turnTotal,
                    newScore = currentPlayer.currentScore + turnTotal
                )
            } else {
                null
            }
        }
    }
}