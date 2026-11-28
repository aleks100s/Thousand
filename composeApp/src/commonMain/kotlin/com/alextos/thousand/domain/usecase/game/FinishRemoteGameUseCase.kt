package com.alextos.thousand.domain.usecase.game

import com.alextos.thousand.domain.models.FinishedGameSaveResult
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.repository.MultiplayerRepository

class FinishRemoteGameUseCase(
    private val multiplayerRepository: MultiplayerRepository
) {
    suspend operator fun invoke(
        game: RemoteGame,
        onGamePersisted: suspend (FinishedGameSaveResult) -> Unit = {},
    ) {
        if (!game.isFinished()) return
        val savedResult = multiplayerRepository.saveGameResult(game) ?: return
        onGamePersisted(savedResult)
    }
}
