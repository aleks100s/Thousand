package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.repository.GameRepository

class SaveTurnUseCase(
    private val repository: GameRepository
) {
    suspend operator fun invoke(
        player: Player,
        rolls: List<DiceRoll>,
        game: Game
    ): Long {
        val turn = Turn(
            player = player,
            rolls = rolls,
            total = rolls.sumOf { it.result },
            effects = emptyList(),
            results = emptyList()
        )
        player.currentScore += turn.total
        repository.saveGame(game)
        return repository.saveTurn(turn, game)
    }
}