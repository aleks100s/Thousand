package com.alextos.thousand.domain.usecase.user

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.repository.GameRepository

class SaveUserUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(
        name: String,
        kind: UserKind = UserKind.LocalUser,
        id: String? = null,
    ) {
        val userName = name.trim()
        val user = if (id == null) {
            User(
                name = userName,
                kind = kind,
            )
        } else {
            User(
                id = id,
                name = userName,
                kind = kind,
            )
        }

        gameRepository.saveUser(
            user,
        )
    }
}
