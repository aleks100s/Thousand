package com.alextos.thousand.domain.usecase.game.crud

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.repository.GameRepository

class CreateRematchUseCase(
    private val repository: GameRepository,
) {
    suspend operator fun invoke(gameId: Long): Game? {
        val sourceGame = repository.getGame(gameId) ?: return null
        val players = sourceGame.players.map { player ->
            Player(user = player.user)
        }

        return repository.createGame(
            Game(
                players = players.shuffled(),
                settings = sourceGame.settings,
            )
        )
    }
}
