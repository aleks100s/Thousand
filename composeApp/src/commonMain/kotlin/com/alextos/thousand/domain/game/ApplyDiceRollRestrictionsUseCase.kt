package com.alextos.thousand.domain.game

import com.alextos.thousand.domain.GameConstants.BARREL_1
import com.alextos.thousand.domain.GameConstants.BARREL_2
import com.alextos.thousand.domain.GameConstants.BARREL_3
import com.alextos.thousand.domain.GameConstants.STARTING_LIMIT
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.RollAbility

class ApplyDiceRollRestrictionsUseCase {
    operator fun invoke(
        rerollAbility: RollAbility,
        currentPlayer: Player,
        game: Game,
        turnTotal: Int,
    ): Boolean {
        if (rerollAbility == RollAbility.UNAVAILABLE) {
            return false
        }

        return currentPlayer.mustPassStartLimit(game, turnTotal) ||
            currentPlayer.isStuckInBarrel(game, turnTotal)
    }

    private fun Player.mustPassStartLimit(game: Game, turnTotal: Int): Boolean {
        return game.hasStartLimit && hasPassedStartLimit.not() && turnTotal < STARTING_LIMIT
    }

    private fun Player.isStuckInBarrel(game: Game, turnTotal: Int): Boolean {
        val proposedScore = currentScore + turnTotal
        return game.isBarrel1Active && currentScore in BARREL_1 && proposedScore in BARREL_1 ||
            game.isBarrel2Active && currentScore in BARREL_2 && proposedScore in BARREL_2 ||
            game.isBarrel3Active && currentScore in BARREL_3 && proposedScore in BARREL_3
    }
}
