package com.alextos.thousand.presentation.game.tutorial_game

import com.alextos.thousand.domain.game.server.GameAction

sealed interface TutorialGameAction {
    data object Initialize: TutorialGameAction
    data class SendGameAction(val action: GameAction): TutorialGameAction
    data object CloseMessage: TutorialGameAction
}
