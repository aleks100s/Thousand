package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SaveTurnUseCaseTest {
    @Test
    fun saveTurnPersistsCalculatedTurn() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = Player(
            id = 1L,
            user = User(id = 1L, name = "Алиса"),
            currentScore = 100,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val savedTurn = useCase(
            currentPlayer = player,
            rolls = listOf(
                DiceRoll(
                    dice = emptyList(),
                    result = 10,
                ),
            ),
            game = game,
        )

        assertEquals(42L, savedTurn.id)
        assertEquals(10, savedTurn.total)
        assertNotNull(repository.savedTurn)
    }

    private class FakeGameRepository : GameRepository {
        var savedTurn: Turn? = null

        override fun getAllGames(): Flow<List<Game>> = flowOf(emptyList())

        override fun getAllUsers(): Flow<List<User>> = flowOf(emptyList())

        override suspend fun saveUser(user: User) = Unit

        override suspend fun createGame(game: Game): Game = game.copy(id = 1L)

        override suspend fun saveGame(game: Game) = Unit

        override suspend fun getGame(id: Long): Game? = null

        override suspend fun getAllTurns(gameID: Long): List<Turn> = emptyList()

        override suspend fun saveTurn(turn: Turn, game: Game): Turn {
            savedTurn = turn
            return turn.copy(id = 42L)
        }

        override suspend fun deleteGame(gameId: Long) = Unit
    }
}

private fun runSuspend(block: suspend () -> Unit) {
    var completionResult: Result<Unit>? = null
    block.startCoroutine(
        object : Continuation<Unit> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                completionResult = result
            }
        },
    )
    completionResult?.getOrThrow() ?: error("Suspended test did not complete synchronously")
}
