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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.random.Random

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
        gameRepository.saveUsers(listOf(alice, bob))
        val playerAlice = Player(
            id = 3L,
            user = alice,
            currentScore = 1000,
            isWinner = true,
        )
        val playerBob = Player(
            id = 4L,
            user = bob,
            currentScore = 780,
            isWinner = false,
        )
        val random = Random(20260416)

        val activeGame = Game(
            id = 0L,
            startedAt = now,
            finishedAt = null,
            players = listOf(playerAlice, playerBob),
        )

        val savedActiveGameId = gameRepository.saveGame(activeGame)
        val savedActiveGame = activeGame.copy(id = savedActiveGameId)

        saveTurns(
            game = savedActiveGame,
            turns = listOf(
                createTurn(
                    order = 1,
                    player = playerAlice,
                    total = 200,
                    effects = randomEffects(
                        random = random,
                        affectedPlayer = activeGame.players[1],
                    ),
                    rollValues = listOf(
                        listOf(
                            DieValue.ONE,
                            DieValue.FIVE,
                            DieValue.FIVE,
                            DieValue.THREE,
                            DieValue.TWO,
                        ),
                    ),
                ),
                createTurn(
                    order = 2,
                    player = playerBob,
                    total = 250,
                    effects = emptyList(),
                    rollValues = listOf(
                        listOf(
                            DieValue.SIX,
                            DieValue.SIX,
                            DieValue.FIVE,
                            DieValue.ONE,
                            DieValue.ONE,
                        ),
                    ),
                ),
                createTurn(
                    order = 3,
                    player = playerAlice,
                    total = 180,
                    effects = randomEffects(
                        random = random,
                        affectedPlayer = activeGame.players[1],
                    ),
                    rollValues = listOf(
                        listOf(
                            DieValue.ONE,
                            DieValue.ONE,
                            DieValue.FOUR,
                            DieValue.THREE,
                        ),
                        listOf(
                            DieValue.FIVE,
                            DieValue.FIVE,
                            DieValue.TWO,
                        ),
                    ),
                ),
                createTurn(
                    order = 4,
                    player = playerBob,
                    total = 170,
                    effects = emptyList(),
                    rollValues = listOf(
                        listOf(
                            DieValue.SIX,
                            DieValue.TWO,
                            DieValue.THREE,
                            DieValue.FIVE,
                        ),
                        listOf(
                            DieValue.ONE,
                            DieValue.FIVE,
                        ),
                    ),
                ),
                createTurn(
                    order = 5,
                    player = playerAlice,
                    total = 140,
                    effects = randomEffects(
                        random = random,
                        affectedPlayer = activeGame.players[1],
                    ),
                    rollValues = listOf(
                        listOf(
                            DieValue.ONE,
                            DieValue.THREE,
                            DieValue.FIVE,
                        ),
                        listOf(
                            DieValue.FIVE,
                            DieValue.SIX,
                        ),
                    ),
                ),
            ),
        )

        val finishedGame = Game(
            id = 0L,
            startedAt = now - 3.hours,
            finishedAt = now - 1.hours,
            players = listOf(playerAlice, playerBob),
        )

        val savedFinishedGameId = gameRepository.saveGame(finishedGame)
        val savedFinishedGame = finishedGame.copy(id = savedFinishedGameId)

        saveTurns(
            game = savedFinishedGame,
            turns = listOf(
                createTurn(
                    order = 1,
                    player = playerAlice,
                    total = 350,
                    effects = emptyList(),
                    rollValues = listOf(
                        listOf(
                            DieValue.ONE,
                            DieValue.ONE,
                            DieValue.ONE,
                            DieValue.FIVE,
                            DieValue.FIVE,
                        ),
                    ),
                ),
                createTurn(
                    order = 2,
                    player = playerBob,
                    total = 600,
                    effects = randomEffects(
                        random = random,
                        affectedPlayer = finishedGame.players[0],
                    ),
                    rollValues = listOf(
                        listOf(
                            DieValue.SIX,
                            DieValue.SIX,
                            DieValue.SIX,
                            DieValue.THREE,
                            DieValue.TWO,
                        ),
                    ),
                ),
                createTurn(
                    order = 3,
                    player = playerAlice,
                    total = 220,
                    effects = randomEffects(
                        random = random,
                        affectedPlayer = finishedGame.players[1],
                    ),
                    rollValues = listOf(
                        listOf(
                            DieValue.ONE,
                            DieValue.FIVE,
                            DieValue.FIVE,
                            DieValue.FOUR,
                        ),
                        listOf(
                            DieValue.ONE,
                            DieValue.ONE,
                            DieValue.THREE,
                        ),
                    ),
                ),
                createTurn(
                    order = 4,
                    player = playerBob,
                    total = 90,
                    effects = emptyList(),
                    rollValues = listOf(
                        listOf(
                            DieValue.FIVE,
                            DieValue.TWO,
                        ),
                        listOf(
                            DieValue.THREE,
                            DieValue.FOUR,
                            DieValue.SIX,
                        ),
                    ),
                ),
                createTurn(
                    order = 5,
                    player = playerAlice,
                    total = 430,
                    effects = randomEffects(
                        random = random,
                        affectedPlayer = finishedGame.players[1],
                    ),
                    rollValues = listOf(
                        listOf(
                            DieValue.ONE,
                            DieValue.ONE,
                            DieValue.ONE,
                            DieValue.ONE,
                            DieValue.FIVE,
                        ),
                        listOf(
                            DieValue.FIVE,
                            DieValue.FIVE,
                            DieValue.TWO,
                            DieValue.THREE,
                        ),
                    ),
                ),
                createTurn(
                    order = 6,
                    player = playerBob,
                    total = 90,
                    effects = randomEffects(
                        random = random,
                        affectedPlayer = finishedGame.players[0],
                    ),
                    rollValues = listOf(
                        listOf(
                            DieValue.ONE,
                            DieValue.TWO,
                            DieValue.THREE,
                        ),
                        listOf(
                            DieValue.FIVE,
                            DieValue.FOUR,
                        ),
                    ),
                ),
            ),
        )
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
            order = order,
            player = player,
            rolls = rollValues.mapIndexed { index, values ->
                createRoll(
                    order = index + 1,
                    values = values,
                )
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

    private fun createRoll(order: Int, values: List<DieValue>): DiceRoll {
        return DiceRoll(
            id = 0L,
            order = order,
            dice = values.mapIndexed { index, value ->
                Die(
                    id = 0L,
                    order = index + 1,
                    value = value,
                )
            },
            result = values.sumOf { it.value },
        )
    }
}
