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
        private const val BOLT_FINE = 100
        private const val STARTING_LIMIT = 50
        private val BARREL_1 = 200..300
        private val BARREL_2 = 600..700
    }

    suspend operator fun invoke(
        currentPlayer: Player,
        rolls: List<DiceRoll>,
        game: Game
    ): Turn {
        val turnTotal = calculateTurnResult(rolls)

        checkStartingLimit(currentPlayer, turnTotal)?.let {
            return saveTurn(currentPlayer, rolls, turnTotal, it.first, listOf(it.second), game)
        }

        checkGameGoal(currentPlayer, turnTotal)?.let {
            return saveTurn(currentPlayer, rolls, turnTotal, listOf(it.first), listOf(it.second), game)
        }

        checkPitFall(currentPlayer, turnTotal)?.let {
            return saveTurn(currentPlayer, rolls, turnTotal, listOf(it.first), listOf(it.second), game)
        }

        checkBarrels(currentPlayer, turnTotal)?.let {
            return saveTurn(currentPlayer, rolls, turnTotal, it.first, it.second, game)
        }

        val (effects, results) = calculateResults(game, currentPlayer, turnTotal)
        return saveTurn(currentPlayer, rolls, turnTotal, effects, results, game)
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
            currentPlayer.boltCount = 0
            val effect = TurnEffect(affectedPlayer = currentPlayer, effect = Effect.WIN)
            val result = TurnResult(player = currentPlayer, scoreChange = turnTotal, newScore = proposedScore)
            return effect to result
        }
        return null
    }

    private fun calculateResults(
        game: Game,
        currentPlayer: Player,
        turnTotal: Int
    ): Pair<List<TurnEffect>, List<TurnResult>> {
        val effects = mutableListOf<TurnEffect>()
        val results = mutableListOf<TurnResult>()

        if (turnTotal == 0) {
            currentPlayer.boltCount += 1
            checkBolt(currentPlayer)?.let { bolt ->
                effects.addAll(bolt.first)
                results.add(bolt.second)
            } ?: run {
                val currentPlayerResult = TurnResult(
                    player = currentPlayer,
                    scoreChange = turnTotal,
                    newScore = currentPlayer.currentScore
                )
                results.add(currentPlayerResult)
            }
        } else {
            currentPlayer.boltCount = 0
            currentPlayer.currentScore += turnTotal
            currentPlayer.hasPassedStartLimit = true
            val currentPlayerResult = TurnResult(
                player = currentPlayer,
                scoreChange = turnTotal,
                newScore = currentPlayer.currentScore
            )
            results.add(currentPlayerResult)
        }

        for (player in game.players) {
            if (player == currentPlayer) {
                continue
            }
            // Обгоны
        }
        return effects to results
    }

    // Barrel, Bolt, Pit check
    private fun checkBarrels(
        currentPlayer: Player,
        turnTotal: Int
    ): Pair<List<TurnEffect>, List<TurnResult>>? {
        val previousScore = currentPlayer.currentScore
        val proposedScore = previousScore + turnTotal
        val firstBarrel = BARREL_1.contains(previousScore) && BARREL_1.contains(proposedScore)
        val secondBarrel = BARREL_2.contains(previousScore) && BARREL_2.contains(proposedScore)
        if (firstBarrel || secondBarrel) {
            val effects = mutableListOf<TurnEffect>()
            val results = mutableListOf<TurnResult>()

            val barrelEffect = TurnEffect(affectedPlayer = currentPlayer, effect = Effect.BARREL_LIMIT)
            effects.add(barrelEffect)
            currentPlayer.boltCount += 1

            checkBolt(currentPlayer)?.let { bolt ->
                effects.addAll(bolt.first)
                results.add(bolt.second)
            } ?: run {
                val barrelResult = TurnResult(player = currentPlayer, scoreChange = 0, newScore = previousScore)
                results.add(barrelResult)
            }

            return effects to results
        }
        return null
    }

    private fun checkStartingLimit(
        currentPlayer: Player,
        turnTotal: Int
    ): Pair<List<TurnEffect>, TurnResult>? {
        val effects = mutableListOf<TurnEffect>()
        if (!currentPlayer.hasPassedStartLimit) {
            if (turnTotal < STARTING_LIMIT) {
                val effect =
                    TurnEffect(affectedPlayer = currentPlayer, effect = Effect.STARTING_LIMIT)
                effects.add(effect)

                var result: TurnResult
                currentPlayer.boltCount += 1
                checkBolt(currentPlayer)?.let { bolt ->
                    effects.addAll(bolt.first)
                    result = bolt.second
                } ?: run {
                    result = TurnResult(player = currentPlayer, scoreChange = 0, newScore = currentPlayer.currentScore)
                }

                return effects to result
            } else {
                return null
            }
        } else {
            return null
        }
    }

    // Bolt, Pit check
    private fun checkBolt(player: Player): Pair<List<TurnEffect>, TurnResult>? {
        if (player.boltCount == 3) {
            val effects = mutableListOf<TurnEffect>()

            player.boltCount = 0
            player.currentScore -= BOLT_FINE
            val effect = TurnEffect(affectedPlayer = player, effect = Effect.TRIPLE_BOLT)
            effects.add(effect)

            var result: TurnResult
            checkPitFall(player, 0)?.let { pitFall ->
                effects.add(pitFall.first)
                result = pitFall.second.copy(scoreChange = pitFall.second.scoreChange - BOLT_FINE)
            } ?: run {
                result = TurnResult(player = player, scoreChange = -BOLT_FINE, newScore = player.currentScore)
            }
            return effects to result
        }
        return null
    }

    private fun checkPitFall(
        player: Player,
        turnTotal: Int
    ): Pair<TurnEffect, TurnResult>? {
        if (player.currentScore + turnTotal == PIT_SCORE) {
            val effect = TurnEffect(affectedPlayer = player, effect = Effect.PIT_FALL)
            val result = TurnResult(player = player, scoreChange = -player.currentScore, newScore = 0)
            player.currentScore = 0
            player.boltCount = 0
            return effect to result
        }
        return null
    }

    private suspend fun saveTurn(
        currentPlayer: Player,
        rolls: List<DiceRoll>,
        turnTotal: Int,
        effects: List<TurnEffect>,
        results: List<TurnResult>,
        game: Game
    ): Turn {
        val turn = Turn(
            player = currentPlayer,
            rolls = rolls,
            total = turnTotal,
            effects = effects,
            results = results
        )
        return repository.saveTurn(turn, game)
    }
}