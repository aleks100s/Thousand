package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.repository.GameRepository

class SaveTurnUseCase(
    private val repository: GameRepository
) {
    suspend operator fun invoke(turn: Turn, game: Game): Long {
        return repository.saveTurn(turn, game)
    }
}