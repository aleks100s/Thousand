package com.alextos.thousand.domain.usecase.game

import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.repository.MultiplayerRepository

class FinishRemoteGameUseCase(
    private val multiplayerRepository: MultiplayerRepository
) {
    suspend operator fun invoke(game: RemoteGame) {
        multiplayerRepository.finishGame(game)
    }
}
