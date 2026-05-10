package com.alextos.thousand.presentation.game.tutorial_game

sealed interface TutorialGameAction {
    data object Initialize : TutorialGameAction
}
