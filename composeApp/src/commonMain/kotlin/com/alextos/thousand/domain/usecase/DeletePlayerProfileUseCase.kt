package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.repository.GameRepository
import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.service.StorageService
import kotlinx.coroutines.flow.first

class DeletePlayerProfileUseCase(
    private val multiplayerRepository: MultiplayerRepository,
    private val accountService: NativeAccountService,
    private val storageService: StorageService,
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke() {
        accountService.deleteAccount(
            deleteOwnedOnlineData = ::deleteOwnedOnlineData,
            clearLocalData = {
                storageService.clear()
                gameRepository.clearLocalData()
            },
        )
    }

    private suspend fun deleteOwnedOnlineData(userId: String) {
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
    }
}
