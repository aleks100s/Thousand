package com.alextos.thousand.presentation.multiplayer.multiplayer_game

import com.alextos.thousand.domain.models.Game

sealed interface MultiplayerGameEvent {
    data object GameDeleted: MultiplayerGameEvent
    data class FinishGame(val game: Game): MultiplayerGameEvent
    data class ShowMessage(val message: String) : MultiplayerGameEvent
}