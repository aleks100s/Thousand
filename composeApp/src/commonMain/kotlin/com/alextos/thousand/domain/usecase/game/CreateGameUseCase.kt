package com.alextos.thousand.domain.usecase.game

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.GameRepository

class CreateGameUseCase(
    private val repository: GameRepository
) {
    suspend operator fun invoke(
        users: Set<User>,
        isShakeEnabled: Boolean = true,
        isVirtualDiceEnabled: Boolean = true,
        isNotificationEnabled: Boolean = true,
    ): Game {
        val players = users.map {
            Player(user = it)
        }
        return repository.createGame(
            Game(
                players = players,
                isShakeEnabled = isShakeEnabled,
                isVirtualDiceEnabled = isVirtualDiceEnabled,
                isNotificationEnabled = isNotificationEnabled,
            )
        )
    }
}
