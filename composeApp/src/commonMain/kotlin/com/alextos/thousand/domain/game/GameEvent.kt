package com.alextos.thousand.domain.game

import com.alextos.thousand.domain.models.Game

interface GameEvent {
    data class Notification(
        val message: String,
    ) : GameEvent

    data class FinishGame(val game: Game): GameEvent
    data class HapticFeedback(val count: Int): GameEvent
}