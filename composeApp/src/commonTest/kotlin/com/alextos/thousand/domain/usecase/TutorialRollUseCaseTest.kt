package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.game.TutorialRollUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class TutorialRollUseCaseTest {
    private val useCase = TutorialRollUseCase()

    @Test
    fun returnsTutorialRollsInScenarioOrder() {
        val expectedRolls = listOf(
            listOf(5, 2, 3, 4, 6),
            listOf(2, 3, 4, 6),
            listOf(5, 5, 5, 2, 3),
            listOf(1, 1, 1, 2, 3),
            listOf(1, 1, 1, 1, 2),
            listOf(1, 1, 1, 5, 2),
            listOf(5, 5, 5, 2, 3),
            listOf(2, 2, 2, 4, 6),
            listOf(3, 4),
            listOf(2, 2, 2, 4, 6),
            listOf(3, 4),
            listOf(2, 3, 4, 6, 2),
            listOf(3, 3, 3, 3, 3),
            listOf(2, 3, 4, 6, 2),
            listOf(5, 5, 5, 2, 3),
            listOf(5, 2),
            listOf(1, 1, 1, 1, 1),
        )

        expectedRolls.forEach { expected ->
            assertEquals(expected, useCase().map { it.value.value })
        }
    }

    @Test
    fun returnsEmptyRollWhenScenarioIsFinished() {
        repeat(SCENARIO_ROLLS_COUNT) {
            useCase()
        }

        assertEquals(emptyList(), useCase())
    }

    @Test
    fun resetStartsScenarioFromFirstRoll() {
        useCase()
        useCase()

        useCase.reset()

        assertEquals(listOf(5, 2, 3, 4, 6), useCase().map { it.value.value })
    }

    private companion object {
        const val SCENARIO_ROLLS_COUNT = 17
    }
}
