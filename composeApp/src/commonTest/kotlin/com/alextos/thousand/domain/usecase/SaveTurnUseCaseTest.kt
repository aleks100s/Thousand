package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.GameConstants
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.GameRepository
import com.alextos.thousand.domain.game.SaveTurnUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveTurnUseCaseTest {
    @Test
    fun saveTurnWhenStartingLimitIsNotPassedAndBoltCountIsLessThanThree() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 0,
            boltCount = 1,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 2)),
            game = game,
        )

        assertEquals(
            expected = listOf(Effect.STARTING_LIMIT),
            actual = turn.effects.map { it.effect },
        )
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(0, turn.results.first().scoreChange)
        assertEquals(0, turn.results.first().newScore)
    }

    @Test
    fun saveTurnWhenStartingLimitIsNotPassedAndBoltCountBecomesThree() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 150,
            boltCount = 2,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 2)),
            game = game,
        )

        assertEquals(
            expected = listOf(Effect.STARTING_LIMIT, Effect.TRIPLE_BOLT),
            actual = turn.effects.map { it.effect },
        )
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(-GameConstants.BOLT_FINE, turn.results.first().scoreChange)
        assertEquals(50, turn.results.first().newScore)
    }

    @Test
    fun saveTurnWhenPlayerExactlyReachesGameGoal() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 950,
            boltCount = 1,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 3)),
            game = game,
        )

        assertEquals(
            expected = listOf(Effect.WIN),
            actual = turn.effects.map { it.effect },
        )
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(50, turn.results.first().scoreChange)
        assertEquals(GameConstants.GAME_GOAL, turn.results.first().newScore)
    }

    @Test
    fun saveTurnWhenPlayerExceedsGameGoal() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 980,
            boltCount = 1,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 3)),
            game = game,
        )

        assertEquals(
            expected = listOf(Effect.WIN),
            actual = turn.effects.map { it.effect },
        )
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(50, turn.results.first().scoreChange)
        assertEquals(1030, turn.results.first().newScore)
    }

    @Test
    fun saveTurnWhenPlayerReachesPitScore() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 505,
            boltCount = 1,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 3)),
            game = game,
        )

        assertEquals(GameConstants.PIT_SCORE, 505 + turn.total)
        assertEquals(
            expected = listOf(Effect.PIT_FALL),
            actual = turn.effects.map { it.effect },
        )
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(-505, turn.results.first().scoreChange)
        assertEquals(0, turn.results.first().newScore)
        assertEquals(0, player.currentScore)
    }

    @Test
    fun saveTurnWhenCalculateResultsPlayerRollsZero() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 100,
            boltCount = 1,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createZeroRoll()),
            game = game,
        )

        assertEquals(0, turn.total)
        assertEquals(emptyList<Effect>(), turn.effects.map { it.effect })
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(0, turn.results.first().scoreChange)
        assertEquals(100, turn.results.first().newScore)
        assertEquals(100, player.currentScore)
        assertEquals(2, player.boltCount)
    }

    @Test
    fun saveTurnWhenCalculateResultsPlayerRollsZeroAndGetsThirdBolt() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 150,
            boltCount = 2,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createZeroRoll()),
            game = game,
        )

        assertEquals(0, turn.total)
        assertEquals(
            expected = listOf(Effect.TRIPLE_BOLT),
            actual = turn.effects.map { it.effect },
        )
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(-GameConstants.BOLT_FINE, turn.results.first().scoreChange)
        assertEquals(50, turn.results.first().newScore)
        assertEquals(50, player.currentScore)
        assertEquals(0, player.boltCount)
    }

    @Test
    fun saveTurnWhenPlayerRollsZeroAndThirdBoltMovesPlayerToPitScore() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = GameConstants.PIT_SCORE + GameConstants.BOLT_FINE,
            boltCount = 2,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createZeroRoll()),
            game = game,
        )

        assertEquals(0, turn.total)
        assertEquals(
            expected = listOf(Effect.BARREL_LIMIT, Effect.TRIPLE_BOLT, Effect.PIT_FALL),
            actual = turn.effects.map { it.effect },
        )
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(
            expected = -(GameConstants.PIT_SCORE + GameConstants.BOLT_FINE),
            actual = turn.results.first().scoreChange,
        )
        assertEquals(0, turn.results.first().newScore)
        assertEquals(0, player.currentScore)
        assertEquals(0, player.boltCount)
    }

    @Test
    fun saveTurnWhenCalculateResultsPlayerRollsMoreThanZeroAndDoesNotOvertakeOtherPlayer() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 100,
            boltCount = 1,
            hasPassedStartLimit = true,
        )
        val otherPlayer = createPlayer(
            id = 2L,
            name = "Боб",
            currentScore = 200,
            boltCount = 0,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player, otherPlayer),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 3)),
            game = game,
        )

        assertEquals(50, turn.total)
        assertEquals(emptyList<Effect>(), turn.effects.map { it.effect })
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(50, turn.results.first().scoreChange)
        assertEquals(150, turn.results.first().newScore)
        assertEquals(150, player.currentScore)
        assertEquals(200, otherPlayer.currentScore)
        assertEquals(0, player.boltCount)
    }

    @Test
    fun saveTurnWhenCalculateResultsPlayerRollsMoreThanZeroAndOvertakesOtherPlayer() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 100,
            boltCount = 1,
            hasPassedStartLimit = true,
        )
        val otherPlayer = createPlayer(
            id = 2L,
            name = "Боб",
            currentScore = 120,
            boltCount = 0,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player, otherPlayer),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 3)),
            game = game,
        )

        assertEquals(50, turn.total)
        assertEquals(
            expected = listOf(Effect.OVERTAKE),
            actual = turn.effects.map { it.effect },
        )
        assertEquals(2, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(50, turn.results.first().scoreChange)
        assertEquals(150, turn.results.first().newScore)
        assertEquals(otherPlayer, turn.results.last().player)
        assertEquals(-GameConstants.OVERTAKE_FINE, turn.results.last().scoreChange)
        assertEquals(70, turn.results.last().newScore)
        assertEquals(150, player.currentScore)
        assertEquals(70, otherPlayer.currentScore)
        assertEquals(0, player.boltCount)
    }

    @Test
    fun saveTurnWhenPlayerHitsFirstBarrelForTheFirstTime() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 240,
            boltCount = 0,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 2)),
            game = game,
        )

        assertEquals(
            expected = listOf(Effect.BARREL_LIMIT),
            actual = turn.effects.map { it.effect },
        )
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(0, turn.results.first().scoreChange)
        assertEquals(240, turn.results.first().newScore)
        assertEquals(240, player.currentScore)
        assertEquals(1, player.boltCount)
    }

    @Test
    fun saveTurnWhenPlayerHitsFirstBarrelForTheThirdTime() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 240,
            boltCount = 2,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 2)),
            game = game,
        )

        assertEquals(
            expected = listOf(Effect.BARREL_LIMIT, Effect.TRIPLE_BOLT),
            actual = turn.effects.map { it.effect },
        )
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(-GameConstants.BOLT_FINE, turn.results.first().scoreChange)
        assertEquals(140, turn.results.first().newScore)
        assertEquals(140, player.currentScore)
        assertEquals(0, player.boltCount)
    }

    @Test
    fun saveTurnWhenPlayerHitsSecondBarrelForTheFirstTime() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 640,
            boltCount = 0,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 2)),
            game = game,
        )

        assertEquals(
            expected = listOf(Effect.BARREL_LIMIT),
            actual = turn.effects.map { it.effect },
        )
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(0, turn.results.first().scoreChange)
        assertEquals(640, turn.results.first().newScore)
        assertEquals(640, player.currentScore)
        assertEquals(1, player.boltCount)
    }

    @Test
    fun saveTurnWhenPlayerHitsSecondBarrelForTheThirdTime() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = 640,
            boltCount = 2,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val turn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 2)),
            game = game,
        )

        assertEquals(
            expected = listOf(Effect.BARREL_LIMIT, Effect.TRIPLE_BOLT),
            actual = turn.effects.map { it.effect },
        )
        assertEquals(1, turn.results.size)
        assertEquals(player, turn.results.first().player)
        assertEquals(-GameConstants.BOLT_FINE, turn.results.first().scoreChange)
        assertEquals(540, turn.results.first().newScore)
        assertEquals(540, player.currentScore)
        assertEquals(0, player.boltCount)
    }

    @Test
    fun saveTurnWhenPlayerStaysOnSecondBarrelForThreeTurnsInARowAndFallsIntoPit() = runSuspend {
        val repository = FakeGameRepository()
        val useCase = SaveTurnUseCase(repository)
        val player = createPlayer(
            currentScore = GameConstants.PIT_SCORE + GameConstants.BOLT_FINE,
            boltCount = 0,
            hasPassedStartLimit = true,
        )
        val game = Game(
            id = 1L,
            players = listOf(player),
        )

        val firstTurn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 2)),
            game = game,
        )
        val secondTurn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 2)),
            game = game,
        )
        val thirdTurn = useCase(
            currentPlayer = player,
            rolls = listOf(createFivesRoll(count = 2)),
            game = game,
        )

        assertEquals(
            expected = listOf(Effect.BARREL_LIMIT),
            actual = firstTurn.effects.map { it.effect },
        )
        assertEquals(
            expected = listOf(Effect.BARREL_LIMIT),
            actual = secondTurn.effects.map { it.effect },
        )
        assertEquals(
            expected = listOf(Effect.BARREL_LIMIT, Effect.TRIPLE_BOLT, Effect.PIT_FALL),
            actual = thirdTurn.effects.map { it.effect },
        )
        assertEquals(1, thirdTurn.results.size)
        assertEquals(player, thirdTurn.results.first().player)
        assertEquals(
            expected = -(GameConstants.PIT_SCORE + GameConstants.BOLT_FINE),
            actual = thirdTurn.results.first().scoreChange,
        )
        assertEquals(0, thirdTurn.results.first().newScore)
        assertEquals(0, player.currentScore)
        assertEquals(0, player.boltCount)
    }

    private fun createPlayer(
        currentScore: Int,
        boltCount: Int,
        hasPassedStartLimit: Boolean = false,
        id: Long = 1L,
        name: String = "Алиса",
    ): Player {
        return Player(
            id = id,
            user = User(id = id, name = name),
            currentScore = currentScore,
            boltCount = boltCount,
            hasPassedStartLimit = hasPassedStartLimit,
        )
    }

    private fun createZeroRoll(): DiceRoll {
        return DiceRoll(
            dice = listOf(Die(value = DieValue.TWO)),
            result = 0,
        )
    }

    private fun createFivesRoll(count: Int): DiceRoll {
        return DiceRoll(
            dice = List(count) {
                Die(value = DieValue.FIVE)
            },
            result = when (count) {
                3 -> 50
                4 -> 100
                5 -> 500
                else -> count * DieValue.FIVE.score
            },
        )
    }

    private class FakeGameRepository : GameRepository {
        override fun getAllGames(): Flow<List<Game>> = flowOf(emptyList())

        override fun getAllUsers(): Flow<List<User>> = flowOf(emptyList())

        override suspend fun saveUser(user: User) = Unit

        override suspend fun createGame(game: Game): Game = game.copy(id = 1L)

        override suspend fun saveGame(game: Game) = Unit

        override suspend fun getGame(id: Long): Game? = null

        override suspend fun getAllTurns(gameID: Long): List<Turn> = emptyList()

        override suspend fun saveTurn(turn: Turn, game: Game): Turn = turn.copy(id = 42L)

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
