package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.domain.models.RollAbility
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateDiceRollScoreUseCaseTest {
    private val useCase = CalculateDiceRollScoreUseCase()

    @Test
    fun calculateScoresForOnesCombinations() {
        assertScoresFor(
            dieValue = DieValue.ONE,
            expectedScores = listOf(10, 20, 100, 200, 1000),
        )
    }

    @Test
    fun calculateScoresForTwosCombinations() {
        assertScoresFor(
            dieValue = DieValue.TWO,
            expectedScores = listOf(0, 0, 20, 40, 200),
        )
    }

    @Test
    fun calculateScoresForThreesCombinations() {
        assertScoresFor(
            dieValue = DieValue.THREE,
            expectedScores = listOf(0, 0, 30, 60, 300),
        )
    }

    @Test
    fun calculateScoresForFoursCombinations() {
        assertScoresFor(
            dieValue = DieValue.FOUR,
            expectedScores = listOf(0, 0, 40, 80, 400),
        )
    }

    @Test
    fun calculateScoresForFivesCombinations() {
        assertScoresFor(
            dieValue = DieValue.FIVE,
            expectedScores = listOf(5, 10, 50, 100, 500),
        )
    }

    @Test
    fun calculateScoresForSixesCombinations() {
        assertScoresFor(
            dieValue = DieValue.SIX,
            expectedScores = listOf(0, 0, 60, 120, 600),
        )
    }

    @Test
    fun calculateRerollAbilityWhenNoDiceScoreInFiveDiceRoll() {
        assertRerollAbilityForFiveDiceRoll(
            scoringDiceCount = 0,
            expectedRerollAbility = RollAbility.UNAVAILABLE,
        )
    }

    @Test
    fun calculateRerollAbilityWhenOneDieScoresInFiveDiceRoll() {
        assertRerollAbilityForFiveDiceRoll(
            scoringDiceCount = 1,
            expectedRerollAbility = RollAbility.AVAILABLE_4,
        )
    }

    @Test
    fun calculateRerollAbilityWhenTwoDiceScoreInFiveDiceRoll() {
        assertRerollAbilityForFiveDiceRoll(
            scoringDiceCount = 2,
            expectedRerollAbility = RollAbility.AVAILABLE_3,
        )
    }

    @Test
    fun calculateRerollAbilityWhenThreeDiceScoreInFiveDiceRoll() {
        assertRerollAbilityForFiveDiceRoll(
            scoringDiceCount = 3,
            expectedRerollAbility = RollAbility.AVAILABLE_2,
        )
    }

    @Test
    fun calculateRerollAbilityWhenFourDiceScoreInFiveDiceRoll() {
        assertRerollAbilityForFiveDiceRoll(
            scoringDiceCount = 4,
            expectedRerollAbility = RollAbility.AVAILABLE_1,
        )
    }

    @Test
    fun calculateRerollAbilityWhenFiveDiceScoreInFiveDiceRoll() {
        assertRerollAbilityForFiveDiceRoll(
            scoringDiceCount = 5,
            expectedRerollAbility = RollAbility.REQUIRED,
        )
    }

    private fun assertScoresFor(
        dieValue: DieValue,
        expectedScores: List<Int>,
    ) {
        expectedScores.forEachIndexed { index, expectedScore ->
            val diceCount = index + 1
            val result = useCase(
                dice = List(diceCount) {
                    Die(value = dieValue)
                },
            )

            assertEquals(
                expected = expectedScore,
                actual = result.score,
                message = "Unexpected score for ${dieValue.name} x$diceCount",
            )
        }
    }

    private fun assertRerollAbilityForFiveDiceRoll(
        scoringDiceCount: Int,
        expectedRerollAbility: RollAbility,
    ) {
        val scoringDice = List(scoringDiceCount) {
            Die(value = DieValue.FIVE)
        }
        val fillerDice = listOf(
            DieValue.TWO,
            DieValue.THREE,
            DieValue.FOUR,
            DieValue.SIX,
        ).take(5 - scoringDiceCount).map { value ->
            Die(value = value)
        }

        val result = useCase(dice = scoringDice + fillerDice)

        assertEquals(
            expected = expectedRerollAbility,
            actual = result.rerollAbility,
            message = "Unexpected reroll ability for $scoringDiceCount scoring dice in five dice roll",
        )
    }
}
