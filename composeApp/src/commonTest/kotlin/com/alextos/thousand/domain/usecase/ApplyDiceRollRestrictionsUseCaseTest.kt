package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.game.ApplyDiceRollRestrictionsUseCase
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.domain.models.User
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplyDiceRollRestrictionsUseCaseTest {
    private val useCase = ApplyDiceRollRestrictionsUseCase()

    @Test
    fun blocksFinishTurnWhenStartLimitIsNotPassed() {
        val player = createPlayer(hasPassedStartLimit = false)
        val isFinishTurnBlocked = useCase(
            rerollAbility = RollAbility.AVAILABLE_4,
            currentPlayer = player,
            game = createGame(player),
            turnTotal = 45,
        )

        assertTrue(isFinishTurnBlocked)
    }

    @Test
    fun doesNotBlockFinishTurnWhenStartLimitRuleIsDisabled() {
        val player = createPlayer(hasPassedStartLimit = false)
        val isFinishTurnBlocked = useCase(
            rerollAbility = RollAbility.AVAILABLE_4,
            currentPlayer = player,
            game = createGame(player, hasStartLimit = false),
            turnTotal = 45,
        )

        assertFalse(isFinishTurnBlocked)
    }

    @Test
    fun doesNotBlockFinishTurnWhenPlayerHasAlreadyPassedStartLimit() {
        val player = createPlayer(hasPassedStartLimit = true)
        val isFinishTurnBlocked = useCase(
            rerollAbility = RollAbility.AVAILABLE_4,
            currentPlayer = player,
            game = createGame(player),
            turnTotal = 45,
        )

        assertFalse(isFinishTurnBlocked)
    }

    @Test
    fun doesNotBlockFinishTurnWhenTurnTotalPassesStartLimit() {
        val player = createPlayer(hasPassedStartLimit = false)
        val isFinishTurnBlocked = useCase(
            rerollAbility = RollAbility.AVAILABLE_4,
            currentPlayer = player,
            game = createGame(player),
            turnTotal = 50,
        )

        assertFalse(isFinishTurnBlocked)
    }

    @Test
    fun doesNotBlockFinishTurnForZeroRoll() {
        val player = createPlayer(hasPassedStartLimit = false)
        val isFinishTurnBlocked = useCase(
            rerollAbility = RollAbility.UNAVAILABLE,
            currentPlayer = player,
            game = createGame(player),
            turnTotal = 0,
        )

        assertFalse(isFinishTurnBlocked)
    }

    @Test
    fun blocksFinishTurnWhenPlayerStaysInActiveFirstBarrel() {
        val player = createPlayer(currentScore = 220, hasPassedStartLimit = true)
        val isFinishTurnBlocked = useCase(
            rerollAbility = RollAbility.AVAILABLE_4,
            currentPlayer = player,
            game = createGame(player, isBarrel1Active = true),
            turnTotal = 25,
        )

        assertTrue(isFinishTurnBlocked)
    }

    @Test
    fun doesNotBlockFinishTurnWhenFirstBarrelRuleIsDisabled() {
        val player = createPlayer(currentScore = 220, hasPassedStartLimit = true)
        val isFinishTurnBlocked = useCase(
            rerollAbility = RollAbility.AVAILABLE_4,
            currentPlayer = player,
            game = createGame(player, isBarrel1Active = false),
            turnTotal = 25,
        )

        assertFalse(isFinishTurnBlocked)
    }

    @Test
    fun doesNotBlockFinishTurnWhenPlayerLeavesBarrel() {
        val player = createPlayer(currentScore = 280, hasPassedStartLimit = true)
        val isFinishTurnBlocked = useCase(
            rerollAbility = RollAbility.AVAILABLE_4,
            currentPlayer = player,
            game = createGame(player, isBarrel1Active = true),
            turnTotal = 25,
        )

        assertFalse(isFinishTurnBlocked)
    }

    @Test
    fun blocksFinishTurnWhenPlayerStaysInActiveSecondBarrel() {
        val player = createPlayer(currentScore = 620, hasPassedStartLimit = true)
        val isFinishTurnBlocked = useCase(
            rerollAbility = RollAbility.AVAILABLE_4,
            currentPlayer = player,
            game = createGame(player, isBarrel2Active = true),
            turnTotal = 25,
        )

        assertTrue(isFinishTurnBlocked)
    }

    @Test
    fun blocksFinishTurnWhenPlayerStaysInActiveThirdBarrel() {
        val player = createPlayer(currentScore = 920, hasPassedStartLimit = true)
        val isFinishTurnBlocked = useCase(
            rerollAbility = RollAbility.AVAILABLE_4,
            currentPlayer = player,
            game = createGame(player, isBarrel3Active = true),
            turnTotal = 25,
        )

        assertTrue(isFinishTurnBlocked)
    }

    private fun createPlayer(
        currentScore: Int = 0,
        hasPassedStartLimit: Boolean = false,
    ): Player {
        return Player(
            user = User(name = "Игрок"),
            currentScore = currentScore,
            hasPassedStartLimit = hasPassedStartLimit,
        )
    }

    private fun createGame(
        player: Player,
        hasStartLimit: Boolean = true,
        isBarrel1Active: Boolean = true,
        isBarrel2Active: Boolean = true,
        isBarrel3Active: Boolean = false,
    ): Game {
        return Game(
            hasStartLimit = hasStartLimit,
            isBarrel1Active = isBarrel1Active,
            isBarrel2Active = isBarrel2Active,
            isBarrel3Active = isBarrel3Active,
            players = listOf(player),
        )
    }
}
