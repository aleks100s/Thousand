package com.alextos.thousand.data.repository

import com.alextos.thousand.data.dao.PlayerDao
import com.alextos.thousand.data.mappers.toDomain
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StatisticsRepositoryImpl(
    private val playerDao: PlayerDao,
) : StatisticsRepository {
    override fun getAllPlayers(): Flow<List<Player>> {
        return playerDao.getAllPlayers().map { players ->
            players.map { it.toDomain() }
        }
    }
}
