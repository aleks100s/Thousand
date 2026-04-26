package com.alextos.thousand.domain.usecase.statistics

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GamesStatisticsUseCase(
    private val statisticsRepository: StatisticsRepository,
) {
    operator fun invoke(): Flow<GamesStatistics> {
        return statisticsRepository.getAllGames().map { games ->
            val finishedGames = games.filter { game -> game.isFinished() }

            GamesStatistics(
                finishedGamesCount = finishedGames.size,
                players = finishedGames.toStatistics(),
            )
        }
    }

    private fun List<Game>.toStatistics(): List<PlayerWithStatistics> {
        return flatMap { game -> game.players }
            .groupBy { player -> player.user }
            .map { (user, players) ->
                val wins = players.count { player -> player.isWinner }
                val games = players.size

                PlayerWithStatistics(
                    userId = user.id,
                    userName = user.name,
                    games = games,
                    wins = wins,
                    losses = games - wins,
                )
            }
            .sortedWith(
                compareByDescending<PlayerWithStatistics> { it.wins }
                    .thenByDescending { it.games }
                    .thenBy { it.userName },
            )
    }
}

data class GamesStatistics(
    val finishedGamesCount: Int,
    val players: List<PlayerWithStatistics>,
)

data class PlayerWithStatistics(
    val userId: Long,
    val userName: String,
    val games: Int,
    val wins: Int,
    val losses: Int,
)
