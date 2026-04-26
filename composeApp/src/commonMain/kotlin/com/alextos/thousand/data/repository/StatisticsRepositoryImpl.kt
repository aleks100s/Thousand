package com.alextos.thousand.data.repository

import com.alextos.thousand.data.dao.GameDao
import com.alextos.thousand.data.dao.TurnDao
import com.alextos.thousand.data.mappers.toDomain
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StatisticsRepositoryImpl(
    private val gameDao: GameDao,
    private val turnDao: TurnDao,
) : StatisticsRepository {
    override fun getAllGames(): Flow<List<Game>> {
        return gameDao.getAllGames().map { games ->
            games.map { it.toDomain() }
        }
    }

    override fun getAllTurns(): Flow<List<Turn>> {
        return turnDao.getAllTurns().map { turns ->
            turns.map { it.toDomain() }
        }
    }
}
