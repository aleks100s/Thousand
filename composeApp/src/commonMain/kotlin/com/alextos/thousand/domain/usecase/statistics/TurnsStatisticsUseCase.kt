package com.alextos.thousand.domain.usecase.statistics

import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TurnsStatisticsUseCase(
    private val statisticsRepository: StatisticsRepository,
) {
    operator fun invoke(): Flow<TurnsStatistics> {
        return statisticsRepository.getAllTurns().map { turns ->
            TurnsStatistics(
                totalTurns = turns.size,
                averageTurn = turns.averageTotal(),
                bestTurn = turns.maxOfOrNull { turn -> turn.total } ?: 0,
                players = turns.toStatistics(),
            )
        }
    }

    private fun List<Turn>.toStatistics(): List<PlayerWithTurnStatistics> {
        return groupBy { turn -> turn.player.user }
            .map { (user, turns) ->
                PlayerWithTurnStatistics(
                    userId = user.id,
                    userName = user.name,
                    turns = turns.size,
                    averageTurn = turns.averageTotal(),
                    bestTurn = turns.maxOfOrNull { turn -> turn.total } ?: 0,
                )
            }
            .sortedWith(
                compareByDescending<PlayerWithTurnStatistics> { it.bestTurn }
                    .thenByDescending { it.averageTurn }
                    .thenBy { it.userName },
            )
    }

    private fun List<Turn>.averageTotal(): Double {
        if (isEmpty()) return 0.0
        return sumOf { turn -> turn.total }.toDouble() / size
    }
}

data class TurnsStatistics(
    val totalTurns: Int,
    val averageTurn: Double,
    val bestTurn: Int,
    val players: List<PlayerWithTurnStatistics>,
)

data class PlayerWithTurnStatistics(
    val userId: Long,
    val userName: String,
    val turns: Int,
    val averageTurn: Double,
    val bestTurn: Int,
)
