package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.GameRepository

class CreateGameUseCase(
    private val repository: GameRepository
) {
    suspend operator fun invoke(users: Set<User>): Long {
        val players = users.map {
            Player(user = it)
        }
        return repository.saveGame(Game(players = players))
    }
}