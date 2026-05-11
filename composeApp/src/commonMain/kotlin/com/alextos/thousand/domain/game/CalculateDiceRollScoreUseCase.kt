package com.alextos.thousand.domain.game

import com.alextos.thousand.domain.models.DiceRollResult
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.domain.models.RollAbility

class CalculateDiceRollScoreUseCase {
    operator fun invoke(dice: List<Die>): DiceRollResult {
        var rerollDiceCount = dice.count()
        var result = 0
        val list = mutableListOf<String>()
        for (die in DieValue.entries) {
            val count = dice.count { it.value == die }
            val score = calculateDieScore(die, count)
            if (score > 0) {
                list.add("$count \uD83C\uDFB2 по (${die.value}) дают $score очков")
                rerollDiceCount -= count
                result += score
            }
        }
        list.add("результат хода: $result очков")
        if (rerollDiceCount == 0) {
            rerollDiceCount = 5
        } else if (result == 0) {
            rerollDiceCount = 0
        }
        return DiceRollResult(
            score = result,
            rerollAbility = RollAbility.entries[rerollDiceCount],
            rollDescription = list.joinToString()
        )
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