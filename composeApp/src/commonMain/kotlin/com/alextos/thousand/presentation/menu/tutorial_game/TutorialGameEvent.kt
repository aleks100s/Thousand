package com.alextos.thousand.presentation.menu.tutorial_game

interface TutorialGameEvent {
    data class ShowMessage(
        val message: String,
        val isReply: Boolean
    ) : TutorialGameEvent
}