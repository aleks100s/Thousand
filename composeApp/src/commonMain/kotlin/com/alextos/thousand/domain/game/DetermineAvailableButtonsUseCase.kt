package com.alextos.thousand.domain.game

import com.alextos.thousand.domain.models.GameButton
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.RollAbility

class DetermineAvailableButtonsUseCase {
    operator fun invoke(
        currentPlayer: Player?,
        isFirstRoll: Boolean,
        isTutorial: Boolean,
        isGameOver: Boolean,
        rollAbility: RollAbility
    ): List<GameButton> {
        if (isGameOver) {
            return listOf(GameButton.FINISH_GAME)
        }

        if (currentPlayer?.isBot() == true) {
            return if (isFirstRoll && isTutorial) listOf(GameButton.BOT_TURN) else emptyList()
        }

        if (isFirstRoll) {
            return listOf(GameButton.ROLL_THE_DICE)
        }

        return when (rollAbility) {
            RollAbility.REQUIRED -> listOf(GameButton.ROLL_AGAIN)
            RollAbility.UNAVAILABLE -> listOf(GameButton.FINISH_TURN)
            else -> listOf(GameButton.FINISH_TURN, GameButton.ROLL_AGAIN)
        }
    }
}