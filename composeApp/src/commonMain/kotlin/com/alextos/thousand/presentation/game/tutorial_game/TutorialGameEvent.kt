package com.alextos.thousand.presentation.game.tutorial_game

interface TutorialGameEvent {
    data class ShowMessage(
        val message: String,
        val isReply: Boolean
    ) : TutorialGameEvent
}