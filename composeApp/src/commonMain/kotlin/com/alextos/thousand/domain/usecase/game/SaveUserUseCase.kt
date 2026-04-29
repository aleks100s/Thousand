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
        id: Long = 0,
        multiplayerToken: String? = null,
    ) {
        gameRepository.saveUser(
            User(
                id = id,
                name = name.trim(),
                kind = kind,
                multiplayerToken = multiplayerToken
            ),
        )
    }
}
