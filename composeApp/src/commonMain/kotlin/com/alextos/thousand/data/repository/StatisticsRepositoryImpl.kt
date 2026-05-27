package com.alextos.thousand.data.repository

import com.alextos.thousand.data.dao.GameDao
import com.alextos.thousand.data.dao.TurnDao
import com.alextos.thousand.data.dao.TurnEffectDao
import com.alextos.thousand.data.dao.UserDao
import com.alextos.thousand.data.mappers.room.toDomain
import com.alextos.thousand.data.mappers.room.toStorageValue
import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StatisticsRepositoryImpl(
    private val gameDao: GameDao,
    private val turnDao: TurnDao,
    private val turnEffectDao: TurnEffectDao,
    private val userDao: UserDao,
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

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { users ->
            users.map { it.toDomain() }
        }
    }

    override fun getTurnEffectCount(userId: String, effect: Effect): Flow<Int> {
        return turnEffectDao.getEffectsCount(
            userId = userId,
            effectType = effect.toStorageValue(),
        )
    }
}
