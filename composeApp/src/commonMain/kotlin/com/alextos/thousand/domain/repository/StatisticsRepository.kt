package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.User
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun getAllGames(): Flow<List<Game>>

    fun getAllTurns(): Flow<List<Turn>>

    fun getAllUsers(): Flow<List<User>>

    fun getTurnEffectCount(userId: String, effect: Effect): Flow<Int>
}
