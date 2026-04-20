package com.alextos.thousand.data.seed

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.TurnEffect
import com.alextos.thousand.domain.models.TurnResult
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class DatabaseSeeder(
    private val gameRepository: GameRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun seedInBackground() {
        scope.launch {
            seedIfNeeded()
        }
    }

    suspend fun seedIfNeeded() {
        val hasGames = gameRepository.getAllGames().first().isNotEmpty()
        val hasUsers = gameRepository.getAllUsers().first().isNotEmpty()
        if (hasGames || hasUsers) return

        val now = Clock.System.now()
        val alice = User(
            id = 1L,
            name = "Алиса",
        )
        val bob = User(
            id = 2L,
            name = "Боб",
        )
        gameRepository.saveUser(alice)
        gameRepository.saveUser(bob)
        val activePlayerAlice = Player(
            id = 3L,
            user = alice,
            currentScore = 655,
            boltCount = 2,
            isWinner = false,
        )
        val activePlayerBob = Player(
            id = 4L,
            user = bob,
            currentScore = 420,
            isWinner = false,
        )
        val random = Random(20260416)

        val activeGame = Game(
            id = 0L,
            startedAt = now,
            finishedAt = null,
            players = listOf(activePlayerAlice, activePlayerBob),
        )

        gameRepository.createGame(activeGame)
    }

    private suspend fun saveTurns(game: Game, turns: List<Turn>) {
        turns.forEach { turn ->
            gameRepository.saveTurn(
                turn = turn,
                game = game,
            )
        }
    }

    private fun createTurn(
        order: Int,
        player: Player,
        total: Int,
        effects: List<TurnEffect>,
        rollValues: List<List<DieValue>>,
    ): Turn {
        return Turn(
            id = 0L,
            player = player,
            rolls = rollValues.map { values ->
                createRoll(values = values)
            },
            total = total,
            effects = effects,
            results = listOf(
                TurnResult(
                    id = 0,
                    player = player,
                    scoreChange = total,
                    newScore = player.currentScore + total,
                ),
                TurnResult(
                    id = 0,
                    player = player,
                    scoreChange = total,
                    newScore = player.currentScore + total,
                )
            ),
        )
    }

    private fun randomEffects(
        random: Random,
        affectedPlayer: Player,
    ): List<TurnEffect> {
        val effectCount = random.nextInt(from = 1, until = 3)

        return List(effectCount) {
            TurnEffect(
                id = 0L,
                affectedPlayer = affectedPlayer,
                effect = Effect.entries[random.nextInt(Effect.entries.size)],
            )
        }
    }

    private fun createRoll(values: List<DieValue>): DiceRoll {
        return DiceRoll(
            id = 0L,
            dice = values.map { value ->
                Die(
                    id = 0L,
                    value = value,
                )
            },
            result = values.sumOf { it.value },
        )
    }
}
