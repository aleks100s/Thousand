package com.alextos.thousand.domain.usecase.statistics

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RollsStatisticsUseCase(
    private val statisticsRepository: StatisticsRepository,
) {
    operator fun invoke(): Flow<RollsStatistics> {
        return statisticsRepository.getAllTurns().map { turns ->
            val rolls = turns.allRolls()

            RollsStatistics(
                totalRolls = rolls.size,
                averageRoll = rolls.averageResult(),
                bestRoll = rolls.maxOfOrNull { roll -> roll.result } ?: 0,
                averageRollChain = turns.averageRollChain(),
                bestRollChain = turns.maxOfOrNull { turn -> turn.rolls.size } ?: 0,
                players = turns.toStatistics(),
            )
        }
    }

    private fun List<Turn>.toStatistics(): List<PlayerWithRollStatistics> {
        return groupBy { turn -> turn.player.user }
            .map { (user, turns) ->
                val rolls = turns.allRolls()

                PlayerWithRollStatistics(
                    userId = user.id,
                    userName = user.name,
                    rolls = rolls.size,
                    averageRoll = rolls.averageResult(),
                    bestRoll = rolls.maxOfOrNull { roll -> roll.result } ?: 0,
                    averageRollChain = turns.averageRollChain(),
                    bestRollChain = turns.maxOfOrNull { turn -> turn.rolls.size } ?: 0,
                )
            }
            .sortedWith(
                compareByDescending<PlayerWithRollStatistics> { it.bestRoll }
                    .thenByDescending { it.averageRoll }
                    .thenByDescending { it.bestRollChain }
                    .thenBy { it.userName },
            )
    }

    private fun List<Turn>.allRolls(): List<DiceRoll> {
        return flatMap { turn -> turn.rolls }
    }

    private fun List<DiceRoll>.averageResult(): Double {
        if (isEmpty()) return 0.0
        return sumOf { roll -> roll.result }.toDouble() / size
    }

    private fun List<Turn>.averageRollChain(): Double {
        if (isEmpty()) return 0.0
        return sumOf { turn -> turn.rolls.size }.toDouble() / size
    }
}

data class RollsStatistics(
    val totalRolls: Int,
    val averageRoll: Double,
    val bestRoll: Int,
    val averageRollChain: Double,
    val bestRollChain: Int,
    val players: List<PlayerWithRollStatistics>,
)

data class PlayerWithRollStatistics(
    val userId: Long,
    val userName: String,
    val rolls: Int,
    val averageRoll: Double,
    val bestRoll: Int,
    val averageRollChain: Double,
    val bestRollChain: Int,
)
