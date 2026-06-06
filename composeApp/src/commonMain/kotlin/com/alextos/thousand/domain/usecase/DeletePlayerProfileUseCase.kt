package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.service.NativeAccountService
import kotlinx.coroutines.flow.first

class DeletePlayerProfileUseCase(
    private val multiplayerRepository: MultiplayerRepository,
    private val accountService: NativeAccountService,
) {
    suspend operator fun invoke(userId: String) {
        if (userId.isBlank()) return

        multiplayerRepository.userLobbies()
            .first()
            .filter { lobby -> lobby.host == userId }
            .forEach { lobby ->
                multiplayerRepository.disconnectFromLobby(lobby.key)
            }

        multiplayerRepository.userGames()
            .first()
            .filter { game -> game.host == userId }
            .forEach { game ->
                multiplayerRepository.deleteGame(game.key)
            }

        accountService.deleteAccount()
    }
}
