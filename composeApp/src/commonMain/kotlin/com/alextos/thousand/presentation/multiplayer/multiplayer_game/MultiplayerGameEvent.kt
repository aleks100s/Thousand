package com.alextos.thousand.presentation.multiplayer.multiplayer_game

sealed interface MultiplayerGameEvent {
    data object GameDeleted: MultiplayerGameEvent
}