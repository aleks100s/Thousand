package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.domain.models.RollAbility
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateDiceRollScoreUseCaseTest {
    private val useCase = CalculateDiceRollScoreUseCase()

    @Test
    fun calculateScoreForSingleOne() {
        val result = useCase(
            dice = listOf(
                Die(value = DieValue.ONE),
            ),
        )

        assertEquals(10, result.score)
        assertEquals(RollAbility.REQUIRED, result.rerollAbility)
    }

    @Test
    fun calculateScoreForNoScoringDice() {
        val result = useCase(
            dice = listOf(
                Die(value = DieValue.TWO),
                Die(value = DieValue.THREE),
                Die(value = DieValue.FOUR),
            ),
        )

        assertEquals(0, result.score)
        assertEquals(RollAbility.UNAVAILABLE, result.rerollAbility)
    }
}
