package com.alextos.thousand.domain.usecase.game

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.GameRepository

class SaveUserUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(name: String) {
        gameRepository.saveUser(
            User(name = name.trim()),
        )
    }
}
