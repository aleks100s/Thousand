package com.alextos.thousand.presentation.menu.multiplayer.multiplayer_game

sealed interface MultiplayerGameEvent {
    data object GameDeleted:
        MultiplayerGameEvent
    data class ShowGameLost(val winnerName: String) :
        MultiplayerGameEvent
    data class ShowMessage(val message: String) :
        MultiplayerGameEvent
    data class Error(val message: String):
        MultiplayerGameEvent
}
