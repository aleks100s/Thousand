package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.game.BotDecision
import com.alextos.thousand.domain.game.MakeBotDecisionUseCase
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.domain.models.User
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MakeBotDecisionUseCaseTest {
    private val useCase = MakeBotDecisionUseCase()

    @Test
    fun botStopsWhenRerollIsUnavailable() = runSuspend {
        val bot = createBot()

        val shouldRoll = useCase(
            rollAbility = RollAbility.UNAVAILABLE,
            bot = bot,
            game = createGame(bot),
            turnTotal = 0,
        ) == BotDecision.CONTINUE

        assertFalse(shouldRoll)
    }

    @Test
    fun botRollsWhenRerollIsRequired() = runSuspend {
        val bot = createBot()

        val shouldRoll = useCase(
            rollAbility = RollAbility.REQUIRED,
            bot = bot,
            game = createGame(bot),
            turnTotal = 0,
        ) == BotDecision.CONTINUE

        assertTrue(shouldRoll)
    }

    @Test
    fun botRollsWhenStartLimitIsNotPassed() = runSuspend {
        val bot = createBot(hasPassedStartLimit = false)

        val shouldRoll = useCase(
            rollAbility = RollAbility.AVAILABLE_4,
            bot = bot,
            game = createGame(bot),
            turnTotal = 45,
        ) == BotDecision.CONTINUE

        assertTrue(shouldRoll)
    }

    @Test
    fun botStopsWhenGameGoalIsReached() = runSuspend {
        val bot = createBot(currentScore = 990)

        val shouldRoll = useCase(
            rollAbility = RollAbility.AVAILABLE_4,
            bot = bot,
            game = createGame(bot),
            turnTotal = 10,
        ) == BotDecision.CONTINUE

        assertFalse(shouldRoll)
    }

    @Test
    fun botRollsWhenScoreBecomesPitScore() = runSuspend {
        val bot = createBot(currentScore = 500)

        val shouldRoll = useCase(
            rollAbility = RollAbility.AVAILABLE_4,
            bot = bot,
            game = createGame(bot),
            turnTotal = 55,
        ) == BotDecision.CONTINUE

        assertTrue(shouldRoll)
    }

    @Test
    fun botRollsWhenStaysInActiveFirstBarrel() = runSuspend {
        val bot = createBot(currentScore = 220)

        val shouldRoll = useCase(
            rollAbility = RollAbility.AVAILABLE_4,
            bot = bot,
            game = createGame(bot, isBarrel1Active = true),
            turnTotal = 25,
        ) == BotDecision.CONTINUE

        assertTrue(shouldRoll)
    }

    @Test
    fun botDoesNotRollBecauseOfInactiveFirstBarrel() = runSuspend {
        val bot = createBot(currentScore = 220)

        val shouldRoll = useCase(
            rollAbility = RollAbility.AVAILABLE_2,
            bot = bot,
            game = createGame(bot, isBarrel1Active = false),
            turnTotal = 25,
        ) == BotDecision.CONTINUE

        assertFalse(shouldRoll)
    }

    @Test
    fun botRollsWhenStaysInActiveSecondBarrel() = runSuspend {
        val bot = createBot(currentScore = 620)

        val shouldRoll = useCase(
            rollAbility = RollAbility.AVAILABLE_4,
            bot = bot,
            game = createGame(bot, isBarrel2Active = true),
            turnTotal = 25,
        ) == BotDecision.CONTINUE

        assertTrue(shouldRoll)
    }

    @Test
    fun botRollsWhenStaysInActiveThirdBarrel() = runSuspend {
        val bot = createBot(currentScore = 920)

        val shouldRoll = useCase(
            rollAbility = RollAbility.AVAILABLE_4,
            bot = bot,
            game = createGame(bot, isBarrel3Active = true),
            turnTotal = 25,
        ) == BotDecision.CONTINUE

        assertTrue(shouldRoll)
    }

    @Test
    fun botStopsWhenItHasTwoBolts() = runSuspend {
        val bot = createBot(boltCount = 2)

        val shouldRoll = useCase(
            rollAbility = RollAbility.AVAILABLE_4,
            bot = bot,
            game = createGame(bot),
            turnTotal = 50,
        ) == BotDecision.CONTINUE

        assertFalse(shouldRoll)
    }

    @Test
    fun botStopsWhenOnlyTwoDiceCanBeRerolled() = runSuspend {
        val bot = createBot()

        val shouldRoll = useCase(
            rollAbility = RollAbility.AVAILABLE_2,
            bot = bot,
            game = createGame(bot),
            turnTotal = 50,
        ) == BotDecision.CONTINUE

        assertFalse(shouldRoll)
    }

    @Test
    fun botStopsWhenTurnTotalReachesOneHundred() = runSuspend {
        val bot = createBot()

        val shouldRoll = useCase(
            rollAbility = RollAbility.AVAILABLE_4,
            bot = bot,
            game = createGame(bot),
            turnTotal = 100,
        ) == BotDecision.CONTINUE

        assertFalse(shouldRoll)
    }

    @Test
    fun botRollsInRegularSafeCase() = runSuspend {
        val bot = createBot()

        val shouldRoll = useCase(
            rollAbility = RollAbility.AVAILABLE_4,
            bot = bot,
            game = createGame(bot),
            turnTotal = 50,
        ) == BotDecision.CONTINUE

        assertTrue(shouldRoll)
    }

    private fun createBot(
        currentScore: Int = 0,
        boltCount: Int = 0,
        hasPassedStartLimit: Boolean = true,
    ): Player {
        return Player(
            user = User(name = "Бот"),
            currentScore = currentScore,
            boltCount = boltCount,
            hasPassedStartLimit = hasPassedStartLimit,
        )
    }

    private fun createGame(
        bot: Player,
        isBarrel1Active: Boolean = true,
        isBarrel2Active: Boolean = true,
        isBarrel3Active: Boolean = false,
    ): Game {
        return Game(
            isBarrel1Active = isBarrel1Active,
            isBarrel2Active = isBarrel2Active,
            isBarrel3Active = isBarrel3Active,
            players = listOf(bot),
        )
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
