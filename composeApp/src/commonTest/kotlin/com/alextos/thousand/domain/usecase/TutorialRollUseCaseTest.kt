package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.usecase.game.CalculateDiceRollScoreUseCase
import com.alextos.thousand.domain.usecase.game.FindCurrentPlayerUseCase
import com.alextos.thousand.domain.usecase.game.SaveTurnUseCase
import com.alextos.thousand.domain.usecase.game.TutorialNextAction
import com.alextos.thousand.domain.usecase.game.TutorialRollUseCase
import com.alextos.thousand.domain.usecase.game.crud.UpdateGameUseCase
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals

class TutorialRollUseCaseTest {
    private val useCase = TutorialRollUseCase()

    @Test
    fun returnsTutorialRollsInScenarioOrder() {
        val expectedRolls = listOf(
            ExpectedTutorialRoll(listOf(5, 2, 3, 4, 6), TutorialNextAction.Reroll, hasAdvice = true),
            ExpectedTutorialRoll(listOf(2, 3, 4, 6), TutorialNextAction.FinishTurn, hasAdvice = true),
            ExpectedTutorialRoll(listOf(5, 5, 5, 2, 3), TutorialNextAction.FinishTurn),
            ExpectedTutorialRoll(listOf(1, 1, 1, 2, 3), TutorialNextAction.FinishTurn, hasAdvice = true),
            ExpectedTutorialRoll(listOf(1, 1, 1, 1, 2), TutorialNextAction.FinishTurn),
            ExpectedTutorialRoll(listOf(1, 1, 1, 1, 2), TutorialNextAction.FinishTurn, hasAdvice = true),
            ExpectedTutorialRoll(listOf(5, 5, 5, 2, 3), TutorialNextAction.FinishTurn),
            ExpectedTutorialRoll(listOf(2, 2, 2, 4, 6), TutorialNextAction.Reroll, hasAdvice = true),
            ExpectedTutorialRoll(listOf(3, 4), TutorialNextAction.FinishTurn, hasAdvice = true),
            ExpectedTutorialRoll(listOf(2, 2, 2, 4, 6), TutorialNextAction.Reroll),
            ExpectedTutorialRoll(listOf(3, 4), TutorialNextAction.FinishTurn),
            ExpectedTutorialRoll(listOf(2, 3, 4, 6, 2), TutorialNextAction.FinishTurn, hasAdvice = true),
            ExpectedTutorialRoll(listOf(3, 3, 3, 3, 3), TutorialNextAction.Reroll),
            ExpectedTutorialRoll(listOf(5, 2, 3, 4, 6), TutorialNextAction.FinishTurn),
            ExpectedTutorialRoll(listOf(2, 3, 4, 6, 2), TutorialNextAction.FinishTurn, hasAdvice = true),
            ExpectedTutorialRoll(listOf(5, 5, 5, 2, 3), TutorialNextAction.FinishTurn),
            ExpectedTutorialRoll(listOf(1, 1, 1, 1, 1), TutorialNextAction.Reroll, hasAdvice = true),
            ExpectedTutorialRoll(listOf(5, 2, 3, 4, 6), TutorialNextAction.FinishTurn, hasAdvice = true),
        )

        expectedRolls.forEach { expected ->
            val roll = useCase()
            assertEquals(expected.dice, roll.dice.map { it.value.value })
            assertEquals(expected.nextAction, roll.nextAction)
            assertEquals(expected.hasAdvice, roll.advice != null)
        }
    }

    @Test
    fun returnsEmptyRollWhenScenarioIsFinished() {
        repeat(SCENARIO_ROLLS_COUNT) {
            useCase()
        }

        val roll = useCase()

        assertEquals(emptyList(), roll.dice)
        assertEquals(null, roll.nextAction)
        assertEquals(null, roll.advice)
    }

    @Test
    fun resetStartsScenarioFromFirstRoll() {
        useCase()
        useCase()

        useCase.reset()

        val roll = useCase()

        assertEquals(listOf(5, 2, 3, 4, 6), roll.dice.map { it.value.value })
        assertEquals(TutorialNextAction.Reroll, roll.nextAction)
        assertEquals(true, roll.advice != null)
    }

    @Test
    fun tutorialScenarioKeepsExpectedScoresWithOvertakeFines() = runSuspend {
        val repository = FakeGameRepository()
        val calculateDiceRollScore = CalculateDiceRollScoreUseCase()
        val saveTurn = SaveTurnUseCase(repository)
        val updateGame = UpdateGameUseCase(repository)
        val findCurrentPlayer = FindCurrentPlayerUseCase()
        val userPlayer = Player(user = User(name = "Ты", kind = UserKind.MainUser))
        val botPlayer = Player(user = User(name = "Бот", kind = UserKind.Bot))
        val game = Game(players = listOf(userPlayer, botPlayer))
        val turns = mutableListOf<Turn>()
        val scores = mutableListOf<Pair<Int, Int>>()
        var currentPlayer = findCurrentPlayer(game, null)

        scenario@ while (game.isFinished().not()) {
            val rolls = mutableListOf<DiceRoll>()
            while (true) {
                val tutorialRoll = useCase()
                if (tutorialRoll.dice.isEmpty()) {
                    break@scenario
                }

                val result = calculateDiceRollScore(tutorialRoll.dice)
                rolls.add(
                    DiceRoll(
                        dice = tutorialRoll.dice,
                        result = result.score,
                    )
                )

                if (tutorialRoll.nextAction == TutorialNextAction.FinishTurn) {
                    break
                }
            }

            val turn = saveTurn(
                currentPlayer = currentPlayer ?: error("Current player is missing"),
                rolls = rolls,
                game = game,
                isTutorial = true,
            )
            turns.add(turn)
            updateGame(game, turn, isTutorial = true)
            scores.add(userPlayer.currentScore to botPlayer.currentScore)
            currentPlayer = findCurrentPlayer(game, turn)
        }

        assertEquals(
            listOf(
                0 to 0,
                0 to 50,
                100 to 0,
                50 to 200,
                250 to 150,
                250 to 200,
                250 to 200,
                250 to 200,
                250 to 200,
                200 to 505,
                100 to 505,
                100 to 0,
                1105 to 0,
            ),
            scores
        )
        assertEquals(13, turns.count())
        assertEquals(listOf(Effect.STARTING_LIMIT), turns[0].effects.map { it.effect })
        assertEquals(listOf(Effect.OVERTAKE), turns[2].effects.map { it.effect })
        assertEquals(listOf(Effect.OVERTAKE), turns[3].effects.map { it.effect })
        assertEquals(listOf(Effect.OVERTAKE), turns[4].effects.map { it.effect })
        assertEquals(listOf(Effect.BARREL_LIMIT), turns[6].effects.map { it.effect })
        assertEquals(listOf(Effect.BARREL_LIMIT), turns[7].effects.map { it.effect })
        assertEquals(listOf(Effect.BARREL_LIMIT), turns[8].effects.map { it.effect })
        assertEquals(listOf(Effect.OVERTAKE), turns[9].effects.map { it.effect })
        assertEquals(listOf(Effect.BARREL_LIMIT, Effect.TRIPLE_BOLT), turns[10].effects.map { it.effect })
        assertEquals(listOf(Effect.PIT_FALL), turns[11].effects.map { it.effect })
        assertEquals(listOf(Effect.WIN), turns[12].effects.map { it.effect })
        assertEquals(true, userPlayer.isWinner)
    }

    private data class ExpectedTutorialRoll(
        val dice: List<Int>,
        val nextAction: TutorialNextAction,
        val hasAdvice: Boolean = false,
    )

    private companion object {
        const val SCENARIO_ROLLS_COUNT = 18
    }

    private class FakeGameRepository : GameRepository {
        override fun getAllGames(): Flow<List<Game>> = flowOf(emptyList())

        override fun getAllUsers(): Flow<List<User>> = flowOf(emptyList())

        override suspend fun getMainUser(): User? = null

        override suspend fun saveUser(user: User) = Unit

        override suspend fun deleteUser(userId: String) = Unit

        override suspend fun createGame(game: Game): Game = game

        override suspend fun saveGame(game: Game) = Unit

        override suspend fun getGame(id: Long): Game? = null

        override suspend fun getAllTurns(gameID: Long): List<Turn> = emptyList()

        override suspend fun saveTurn(turn: Turn, game: Game): Turn = turn

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
