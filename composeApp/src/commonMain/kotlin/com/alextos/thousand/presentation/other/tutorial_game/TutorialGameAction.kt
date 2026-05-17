package com.alextos.thousand.presentation.other.tutorial_game

import com.alextos.thousand.domain.usecase.game.server.GameAction

sealed interface TutorialGameAction {
    data object Initialize: TutorialGameAction
    data class SendGameAction(val action: GameAction): TutorialGameAction
    data object CloseMessage: TutorialGameAction
}
