package com.alextos.thousand.domain.usecase.user

import com.alextos.thousand.domain.models.PlayerAvatar
import com.alextos.thousand.domain.models.PlayerColor
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.repository.GameRepository
import kotlinx.coroutines.flow.first

class SaveUserUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(
        name: String,
        kind: UserKind = UserKind.LocalUser,
        id: String? = null,
        avatar: PlayerAvatar = PlayerAvatar.Neutral,
    ) {
        val userName = name.trim()
        val color = if (kind == UserKind.MainUser) PlayerColor.Gold else {
            val usedColors = gameRepository.getAllUsers().first().map { it.color }.toSet()
            val palette = PlayerColor.entries.filter { it != PlayerColor.Gold }
            palette.filterNot { it in usedColors }.ifEmpty { palette }.random()
        }
        val user = if (id == null) {
            User(
                name = userName,
                kind = kind,
                avatar = avatar,
                color = color,
            )
        } else {
            User(
                id = id,
                name = userName,
                kind = kind,
                avatar = avatar,
                color = color,
            )
        }

        gameRepository.saveUser(
            user,
        )
    }
}
