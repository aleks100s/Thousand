package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow

class GetAllUsersUseCase(
    private val gameRepository: GameRepository,
) {
    operator fun invoke(): Flow<List<User>> = gameRepository.getAllUsers()
}
