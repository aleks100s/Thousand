package com.alextos.thousand.domain.usecase.user

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.GameRepository

class UpdateUserUseCase(
    private val repository: GameRepository,
) {
    suspend operator fun invoke(user: User) {
        repository.saveUser(user)
    }
}
