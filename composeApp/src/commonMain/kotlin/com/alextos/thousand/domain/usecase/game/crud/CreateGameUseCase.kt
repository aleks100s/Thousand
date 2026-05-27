package com.alextos.thousand.domain.usecase.game.crud

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.GameRepository

class CreateGameUseCase(
    private val repository: GameRepository
) {
    suspend operator fun invoke(
        users: Set<User>,
        settings: GameSettings = GameSettings(),
    ): Game {
        val players = users.map {
            Player(user = it)
        }
        return repository.createGame(
            Game(
                players = players.shuffled(),
                settings = settings,
            )
        )
    }
}
