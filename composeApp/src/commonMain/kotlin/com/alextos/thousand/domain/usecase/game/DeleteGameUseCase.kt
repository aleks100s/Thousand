package com.alextos.thousand.domain.usecase.game

import com.alextos.thousand.domain.repository.GameRepository

class DeleteGameUseCase(
    private val repository: GameRepository,
) {
    suspend operator fun invoke(gameId: Long) {
        repository.deleteGame(gameId)
    }
}
