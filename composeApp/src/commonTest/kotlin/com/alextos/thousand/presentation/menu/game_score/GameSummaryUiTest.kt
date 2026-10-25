package com.alextos.thousand.presentation.menu.game_score

import com.alextos.thousand.domain.models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameSummaryUiTest {
    private val alice = Player(user = User(id = "a", name = "Маша"), currentScore = 1000)
    private val bob = Player(user = User(id = "b", name = "Саша"), currentScore = 150)
    private val game = Game(players = listOf(alice, bob))

    @Test
    fun summaryIsOnlyVisibleForFinishedGames() {
        assertFalse(GameScoreState().showSummary)
        assertFalse(GameScoreState(game = game).showSummary)
        assertTrue(GameScoreState(game = game.copy(players = listOf(alice.copy(isWinner = true), bob))).showSummary)
    }

    @Test
    fun chartTracksAllPlayersIncludingPenaltiesByUserId() {
        val turns = listOf(
            turn(alice, 100, listOf(TurnResult(player = alice.copy(currentScore = 0), scoreChange = 100, newScore = 100))),
            turn(bob, 200, listOf(
                TurnResult(player = bob.copy(currentScore = 0), scoreChange = 200, newScore = 200),
                TurnResult(player = alice.copy(currentScore = 100), scoreChange = -50, newScore = 50),
            )),
        )
        val chart = game.buildScoreSeries(turns)
        assertEquals(listOf(0, 100, 50), chart[0].points)
        assertEquals(listOf(0, 0, 200), chart[1].points)
    }

    @Test
    fun averagesIncludeZeroTurnsAndPreserveGlobalComparison() {
        val turns = listOf(
            turn(alice, 100, emptyList(), listOf(25, 75)),
            turn(alice.copy(currentScore = 0), 0, emptyList(), listOf(0)),
        )
        val summary = game.buildPlayerStatistics(turns, mapOf("a" to 40.0), mapOf("a" to 20.0))
        assertEquals(50.0, summary[0].gameAverageTurn)
        assertEquals(100.0 / 3, summary[0].gameAverageRoll)
        assertEquals(40.0, summary[0].globalAverageTurn)
        assertEquals(20.0, summary[0].globalAverageRoll)
        assertEquals(0.0, summary[1].gameAverageTurn)
        assertEquals(0.0, summary[1].gameAverageRoll)
    }

    private fun turn(player: Player, total: Int, results: List<TurnResult>, rolls: List<Int> = emptyList()) =
        Turn(player = player, rolls = rolls.map { DiceRoll(dice = emptyList(), result = it) },
            total = total, effects = emptyList(), results = results)
}
