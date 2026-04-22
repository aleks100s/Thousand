package com.alextos.thousand.domain.usecase.game

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow

class GetAllGamesUseCase(
    private val gameRepository: GameRepository,
) {
    operator fun invoke(): Flow<List<Game>> = gameRepository.getAllGames()
}
