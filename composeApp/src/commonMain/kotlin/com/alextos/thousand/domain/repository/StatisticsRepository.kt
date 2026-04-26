package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Turn
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun getAllGames(): Flow<List<Game>>

    fun getAllTurns(): Flow<List<Turn>>
}
