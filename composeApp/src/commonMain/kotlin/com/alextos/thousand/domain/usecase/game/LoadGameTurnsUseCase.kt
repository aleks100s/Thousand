package com.alextos.thousand.domain.usecase.game

import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.repository.GameRepository

class LoadGameTurnsUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(gameId: Long): List<Turn> = gameRepository.getAllTurns(gameId)
}
