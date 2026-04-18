package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.DiceRollResult
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.domain.models.RollAbility

class CalculateDiceRollScoreUseCase {
    operator fun invoke(dice: List<Die>): DiceRollResult {
        var rerollDiceCount = dice.count()
        var result = 0
        for (die in DieValue.entries) {
            val count = dice.count { it.value == die }
            val score = calculateDieScore(die, count)
            if (score > 0) {
                rerollDiceCount -= count
                result += score
            }
        }
        if (rerollDiceCount == 0) {
            rerollDiceCount = 5
        } else if (result == 0) {
            rerollDiceCount = 0
        }
        return DiceRollResult(score = result, rerollAbility = RollAbility.entries[rerollDiceCount])
    }

    private fun calculateDieScore(die: DieValue, count: Int): Int {
        return when (count) {
            3 -> die.score * 10
            4 -> die.score * 20
            5 -> die.score * 100
            else -> when (die) {
                DieValue.ONE, DieValue.FIVE -> die.score * count
                else -> 0
            }
        }
    }
}
