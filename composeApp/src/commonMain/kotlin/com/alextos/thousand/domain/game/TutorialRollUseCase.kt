package com.alextos.thousand.domain.game

import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue

class TutorialRollUseCase {
    private var index = 0

    operator fun invoke(): List<Die> {
        val dice = tutorialRolls.getOrNull(index).orEmpty()
        index += 1
        return dice.map { value ->
            Die(value = value)
        }
    }

    fun reset() {
        index = 0
    }

    private companion object {
        val tutorialRolls = listOf(
            listOf(DieValue.FIVE, DieValue.TWO, DieValue.THREE, DieValue.FOUR, DieValue.SIX),
            listOf(DieValue.TWO, DieValue.THREE, DieValue.FOUR, DieValue.SIX),
            listOf(DieValue.FIVE, DieValue.FIVE, DieValue.FIVE, DieValue.TWO, DieValue.THREE),
            listOf(DieValue.ONE, DieValue.ONE, DieValue.ONE, DieValue.TWO, DieValue.THREE),
            listOf(DieValue.ONE, DieValue.ONE, DieValue.ONE, DieValue.ONE, DieValue.TWO),
            listOf(DieValue.ONE, DieValue.ONE, DieValue.ONE, DieValue.FIVE, DieValue.TWO),
            listOf(DieValue.FIVE, DieValue.FIVE, DieValue.FIVE, DieValue.TWO, DieValue.THREE),
            listOf(DieValue.TWO, DieValue.TWO, DieValue.TWO, DieValue.FOUR, DieValue.SIX),
            listOf(DieValue.THREE, DieValue.FOUR),
            listOf(DieValue.TWO, DieValue.TWO, DieValue.TWO, DieValue.FOUR, DieValue.SIX),
            listOf(DieValue.THREE, DieValue.FOUR),
            listOf(DieValue.TWO, DieValue.THREE, DieValue.FOUR, DieValue.SIX, DieValue.TWO),
            listOf(DieValue.THREE, DieValue.THREE, DieValue.THREE, DieValue.THREE, DieValue.THREE),
            listOf(DieValue.TWO, DieValue.THREE, DieValue.FOUR, DieValue.SIX, DieValue.TWO),
            listOf(DieValue.FIVE, DieValue.FIVE, DieValue.FIVE, DieValue.TWO, DieValue.THREE),
            listOf(DieValue.FIVE, DieValue.TWO),
            listOf(DieValue.ONE, DieValue.ONE, DieValue.ONE, DieValue.ONE, DieValue.ONE),
        )
    }
}
