package com.alextos.thousand.presentation.game.play_game

sealed interface PlayGameAction {
    data object LoadGame : PlayGameAction
}
