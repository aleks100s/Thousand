package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.repository.GameRepository

class LoadGameUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(gameId: Long): Game? = gameRepository.getGame(gameId)
}
