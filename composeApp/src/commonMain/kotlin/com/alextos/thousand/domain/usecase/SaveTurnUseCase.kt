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
        const val GAME_GOAL = 1000
        private const val PIT_SCORE = 555
        private val BARREL_1 = 200..300
        private val BARREL_2 = 600..700
    }

    suspend operator fun invoke(
        currentPlayer: Player,
        rolls: List<DiceRoll>,
        game: Game
    ): Turn {
        val turnTotal = calculateTurnResult(rolls)

        checkGameGoal(currentPlayer, turnTotal)?.let {
            return saveTurn(currentPlayer, rolls, turnTotal, it, game)
        }

        checkPitFall(currentPlayer, turnTotal)?.let {
            return saveTurn(currentPlayer, rolls, turnTotal, it, game)
        }

        checkBarrels(currentPlayer, turnTotal)?.let {
            return saveTurn(currentPlayer, rolls, turnTotal, it, game)
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

    private fun checkGameGoal(
        currentPlayer: Player,
        turnTotal: Int
    ): Pair<TurnEffect, TurnResult>? {
        val proposedScore = currentPlayer.currentScore + turnTotal
        if (proposedScore >= GAME_GOAL) {
            currentPlayer.currentScore = proposedScore
            val effect = TurnEffect(affectedPlayer = currentPlayer, effect = Effect.WIN)
            val result = TurnResult(player = currentPlayer, scoreChange = turnTotal, newScore = proposedScore)
            return effect to result
        }
        return null
    }

    private fun checkPitFall(
        player: Player,
        turnTotal: Int
    ): Pair<TurnEffect, TurnResult>? {
        if (player.currentScore + turnTotal == PIT_SCORE) {
            player.currentScore = 0
            val effect = TurnEffect(affectedPlayer = player, effect = Effect.PIT_FALL)
            val result = TurnResult(player = player, scoreChange = -player.currentScore, newScore = 0)
            return effect to result
        }
        return null
    }

    private fun checkBarrels(
        currentPlayer: Player,
        turnTotal: Int
    ): Pair<TurnEffect, TurnResult>? {
        val previousScore = currentPlayer.currentScore
        val proposedScore = previousScore + turnTotal
        val firstBarrel = BARREL_1.contains(previousScore) && BARREL_1.contains(proposedScore)
        val secondBarrel = BARREL_2.contains(previousScore) && BARREL_2.contains(proposedScore)
        if (firstBarrel || secondBarrel) {
            val effect = TurnEffect(affectedPlayer = currentPlayer, effect = Effect.BARREL_LIMIT)
            val result = TurnResult(player = currentPlayer, scoreChange = 0, newScore = previousScore)
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
                player.currentScore += turnTotal
                TurnResult(
                    player = currentPlayer,
                    scoreChange = turnTotal,
                    newScore = currentPlayer.currentScore
                )
            } else {
                null
            }
        }
    }

    private suspend fun saveTurn(
        currentPlayer: Player,
        rolls: List<DiceRoll>,
        turnTotal: Int,
        pair: Pair<TurnEffect, TurnResult>,
        game: Game
    ): Turn {
        val turn = Turn(
            player = currentPlayer,
            rolls = rolls,
            total = turnTotal,
            effects = listOf(pair.first),
            results = listOf(pair.second)
        )
        return repository.saveTurn(turn, game)
    }
}