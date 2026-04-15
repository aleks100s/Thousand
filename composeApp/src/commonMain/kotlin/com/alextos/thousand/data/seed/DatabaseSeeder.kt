package com.alextos.thousand.data.seed

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

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

        val activeGame = Game(
            id = 0L,
            startedAt = now,
            finishedAt = null,
            players = listOf(
                Player(
                    id = 1L,
                    user = alice,
                    currentScore = 350,
                    isWinner = false,
                ),
                Player(
                    id = 2L,
                    user = bob,
                    currentScore = 420,
                    isWinner = false,
                ),
            ),
        )

        val savedActiveGameId = gameRepository.saveGame(activeGame)
        val savedActiveGame = activeGame.copy(id = savedActiveGameId)

        gameRepository.saveTurn(
            turn = Turn(
                id = 0L,
                order = 1,
                user = alice,
                rolls = listOf(
                    createRoll(
                        values = listOf(
                            DieValue.ONE,
                            DieValue.FIVE,
                            DieValue.FIVE,
                            DieValue.THREE,
                            DieValue.TWO,
                        ),
                    ),
                ),
                total = 200,
                effects = emptyList(),
            ),
            game = savedActiveGame,
        )

        gameRepository.saveTurn(
            turn = Turn(
                id = 0L,
                order = 2,
                user = bob,
                rolls = listOf(
                    createRoll(
                        values = listOf(
                            DieValue.SIX,
                            DieValue.SIX,
                            DieValue.FIVE,
                            DieValue.ONE,
                            DieValue.ONE,
                        ),
                    ),
                ),
                total = 250,
                effects = emptyList(),
            ),
            game = savedActiveGame,
        )

        val finishedGame = Game(
            id = 0L,
            startedAt = now - 3.hours,
            finishedAt = now - 1.hours,
            players = listOf(
                Player(
                    id = 3L,
                    user = alice,
                    currentScore = 1000,
                    isWinner = true,
                ),
                Player(
                    id = 4L,
                    user = bob,
                    currentScore = 780,
                    isWinner = false,
                ),
            ),
        )

        val savedFinishedGameId = gameRepository.saveGame(finishedGame)
        val savedFinishedGame = finishedGame.copy(id = savedFinishedGameId)

        gameRepository.saveTurn(
            turn = Turn(
                id = 0L,
                order = 1,
                user = alice,
                rolls = listOf(
                    createRoll(
                        values = listOf(
                            DieValue.ONE,
                            DieValue.ONE,
                            DieValue.ONE,
                            DieValue.FIVE,
                            DieValue.FIVE,
                        ),
                    ),
                ),
                total = 350,
                effects = emptyList(),
            ),
            game = savedFinishedGame,
        )

        gameRepository.saveTurn(
            turn = Turn(
                id = 0L,
                order = 2,
                user = bob,
                rolls = listOf(
                    createRoll(
                        values = listOf(
                            DieValue.SIX,
                            DieValue.SIX,
                            DieValue.SIX,
                            DieValue.THREE,
                            DieValue.TWO,
                        ),
                    ),
                ),
                total = 600,
                effects = emptyList(),
            ),
            game = savedFinishedGame,
        )
    }

    private fun createRoll(values: List<DieValue>): DiceRoll {
        return DiceRoll(
            id = 0L,
            order = 1,
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
