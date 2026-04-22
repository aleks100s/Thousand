package com.alextos.thousand.presentation.game.play_game

sealed interface PlayGameEvent {
    data class ShowSnackbar(
        val message: String,
    ) : PlayGameEvent
}
