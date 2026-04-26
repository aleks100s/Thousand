package com.alextos.thousand.presentation.game.play_game

import com.alextos.thousand.domain.models.Game

sealed interface PlayGameEvent {
    data class ShowSnackbar(
        val message: String,
    ) : PlayGameEvent

    data class FinishGame(val game: Game): PlayGameEvent
}
