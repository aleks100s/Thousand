package com.alextos.thousand.presentation.menu.play_game

import com.alextos.thousand.domain.models.Game

sealed interface PlayGameEvent {
    data class ShowMessage(
        val message: String,
        val isReply: Boolean
    ) : PlayGameEvent

    data class FinishGame(val game: Game): PlayGameEvent
}
