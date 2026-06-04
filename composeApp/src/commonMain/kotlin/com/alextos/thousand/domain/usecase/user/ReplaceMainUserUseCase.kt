package com.alextos.thousand.domain.usecase.user

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.GameRepository

class ReplaceMainUserUseCase(
    private val repository: GameRepository
) {
    suspend operator fun invoke(previous: User, new: User) {
        repository.replaceUser(previous, new)
    }
}
