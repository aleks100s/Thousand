package com.alextos.thousand.domain.usecase.statistics

import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiceStatisticsUseCase(
    private val statisticsRepository: StatisticsRepository,
) {
    operator fun invoke(): Flow<DiceStatistics> {
        return statisticsRepository.getAllTurns().map { turns ->
            val dice = turns.allDice()

            DiceStatistics(
                totalDice = dice.size,
                averageDie = dice.averageValue(),
                distribution = dice.toDistribution(),
                players = turns.toStatistics(),
            )
        }
    }

    private fun List<Turn>.toStatistics(): List<PlayerWithDiceStatistics> {
        return groupBy { turn -> turn.player.user }
            .map { (user, turns) ->
                val dice = turns.allDice()

                PlayerWithDiceStatistics(
                    userId = user.id,
                    userName = user.name,
                    dice = dice.size,
                    averageDie = dice.averageValue(),
                    distribution = dice.toDistribution(),
                )
            }
            .sortedWith(
                compareByDescending<PlayerWithDiceStatistics> { it.dice }
                    .thenByDescending { it.averageDie }
                    .thenBy { it.userName },
            )
    }

    private fun List<Turn>.allDice(): List<Die> {
        return flatMap { turn ->
            turn.rolls.flatMap { roll -> roll.dice }
        }
    }

    private fun List<Die>.averageValue(): Double {
        if (isEmpty()) return 0.0
        return sumOf { die -> die.value.value }.toDouble() / size
    }

    private fun List<Die>.toDistribution(): List<DieValueDistribution> {
        return (1..6).map { value ->
            DieValueDistribution(
                value = value,
                count = count { die -> die.value.value == value },
            )
        }
    }
}

data class DiceStatistics(
    val totalDice: Int,
    val averageDie: Double,
    val distribution: List<DieValueDistribution>,
    val players: List<PlayerWithDiceStatistics>,
)

data class PlayerWithDiceStatistics(
    val userId: Long,
    val userName: String,
    val dice: Int,
    val averageDie: Double,
    val distribution: List<DieValueDistribution>,
)

data class DieValueDistribution(
    val value: Int,
    val count: Int,
)
