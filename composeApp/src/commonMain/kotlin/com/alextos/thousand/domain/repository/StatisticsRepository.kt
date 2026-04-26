package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.Player
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun getAllPlayers(): Flow<List<Player>>
}
