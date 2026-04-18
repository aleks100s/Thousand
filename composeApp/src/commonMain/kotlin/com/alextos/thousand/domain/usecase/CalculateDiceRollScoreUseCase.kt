package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.DiceRollResult
import com.alextos.thousand.domain.models.DieValue

class CalculateDiceRollScoreUseCase {
    operator fun invoke(roll: DiceRoll): DiceRollResult {
        var rerollDiceCount = 6
        var result = 0
        for (die in DieValue.entries) {
            val count = roll.dice.count { it.value == die }
            val score = calculateDieScore(DieValue.ONE, count)
            if (score > 0) {
                rerollDiceCount -= count
                result += score
            }
        }
        return DiceRollResult(score = result, rerollDiceCount = if (result == 0) 0 else rerollDiceCount)
    }

    private fun calculateDieScore(die: DieValue, count: Int): Int {
        return when (count) {
            3 -> die.value * 10
            4 -> die.value * 20
            5 -> die.value * 100
            else -> when (die) {
                DieValue.ONE, DieValue.FIVE -> die.value * count
                else -> 0
            }
        }
    }
}
