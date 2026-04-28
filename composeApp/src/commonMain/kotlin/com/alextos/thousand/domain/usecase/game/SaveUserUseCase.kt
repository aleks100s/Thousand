package com.alextos.thousand.domain.usecase.game

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.repository.GameRepository

class SaveUserUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(
        name: String,
        kind: UserKind = UserKind.LocalUser,
    ) {
        gameRepository.saveUser(
            User(
                name = name.trim(),
                kind = kind,
            ),
        )
    }
}
