package com.alextos.thousand.presentation.menu.game_score

import com.alextos.thousand.domain.models.*
import com.alextos.thousand.domain.usecase.game.FormatTurnEffectUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class TurnHistoryUiTest {
    private val player = Player(user = User(id = "current", name = "Маша"))
    private val opponent = Player(user = User(id = "other", name = "Саша"))
    private val formatter = FormatTurnEffectUseCase()

    @Test
    fun highlightsOnlyScoringDiceIncludingTriples() {
        val turn = Turn(player = player, rolls = listOf(
            DiceRoll(dice = listOf(1, 5, 2, 2, 3).map { Die(value = DieValue.entries[it - 1]) }, result = 15),
            DiceRoll(dice = listOf(6, 6, 6, 4).map { Die(value = DieValue.entries[it - 1]) }, result = 60),
        ), total = 75, effects = emptyList(), results = emptyList())
        val ui = turn.toHistoryUi(3, formatter)
        assertEquals(listOf(true, true, false, false, false), ui.rolls[0].dice.map { it.scoring })
        assertEquals(listOf(true, true, true, false), ui.rolls[1].dice.map { it.scoring })
        assertEquals(listOf(15, 60), ui.rolls.map { it.score })
        assertEquals(3, ui.number)
    }

    @Test
    fun preservesCancelledTurnAndAllAffectedPlayerScores() {
        val effect = TurnEffect(affectedPlayer = player, effect = Effect.STARTING_LIMIT)
        val turn = Turn(id = 42, player = player,
            rolls = listOf(DiceRoll(dice = listOf(Die(value = DieValue.FIVE)), result = 5)),
            total = 0, effects = listOf(effect), results = listOf(
                TurnResult(player = opponent, scoreChange = -50, newScore = 250),
                TurnResult(player = player.copy(currentScore = 100), scoreChange = 0, newScore = 100),
            ))
        val ui = turn.toHistoryUi(1, formatter)
        assertEquals(0, ui.total)
        assertEquals(100, ui.playerScore)
        assertEquals(listOf(formatter(effect, player)), ui.effects)
        assertEquals(listOf(HistoryScoreChangeUi("Саша", -50, 250)), ui.otherChanges)
    }
}
