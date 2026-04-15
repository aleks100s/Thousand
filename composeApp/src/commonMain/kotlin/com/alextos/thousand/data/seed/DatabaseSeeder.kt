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

        val alice = User(
            id = 1L,
            name = "Алиса",
        )
        val bob = User(
            id = 2L,
            name = "Боб",
        )
        gameRepository.saveUsers(listOf(alice, bob))

        val game = Game(
            id = 0L,
            startedAt = Clock.System.now(),
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

        val savedGameId = gameRepository.saveGame(game)
        val savedGame = game.copy(id = savedGameId)

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
            game = savedGame,
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
            game = savedGame,
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
