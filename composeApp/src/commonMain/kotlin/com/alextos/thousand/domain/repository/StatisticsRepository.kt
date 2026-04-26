package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.Game
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun getAllGames(): Flow<List<Game>>
}
