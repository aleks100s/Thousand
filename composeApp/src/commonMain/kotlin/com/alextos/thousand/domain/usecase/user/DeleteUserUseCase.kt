package com.alextos.thousand.domain.usecase.user

import com.alextos.thousand.domain.repository.GameRepository

class DeleteUserUseCase(
    private val repository: GameRepository,
) {
    suspend operator fun invoke(userId: Long) {
        repository.deleteUser(userId)
    }
}