package com.alextos.thousand.domain.game

import com.alextos.thousand.domain.GameConstants.BARREL_1
import com.alextos.thousand.domain.GameConstants.BARREL_2
import com.alextos.thousand.domain.GameConstants.BARREL_3
import com.alextos.thousand.domain.GameConstants.GAME_GOAL
import com.alextos.thousand.domain.GameConstants.PIT_SCORE
import com.alextos.thousand.domain.GameConstants.STARTING_LIMIT
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.RollAbility
import kotlinx.coroutines.delay

class MakeBotRollUseCase {
    suspend operator fun invoke(
        rollAbility: RollAbility,
        bot: Player,
        game: Game,
        turnTotal: Int
    ): Boolean {
        delay(1500L)
        when (rollAbility) {
            RollAbility.UNAVAILABLE -> return false
            RollAbility.REQUIRED -> return true
            else -> {
                if (bot.hasPassedStartLimit.not() && turnTotal < STARTING_LIMIT) return true
                if (bot.currentScore + turnTotal >= GAME_GOAL) return false
                if (bot.currentScore + turnTotal == PIT_SCORE) return true
                if (bot.isInBarrel(game, turnTotal)) return true
                if (bot.boltCount == DANGEROUS_BOLT_COUNT) return false
                if (rollAbility.count <= SAFE_STOP_DICE_COUNT) return false

                return true
            }
        }
    }

    private fun Player.isInBarrel(game: Game, turnTotal: Int): Boolean {
        val score = currentScore + turnTotal
        return game.isBarrel1Active && score in BARREL_1 && currentScore in BARREL_1 ||
            game.isBarrel2Active && score in BARREL_2 && currentScore in BARREL_2 ||
            game.isBarrel3Active && score in BARREL_3 && currentScore in BARREL_3
    }

    private companion object {
        const val DANGEROUS_BOLT_COUNT = 2
        const val SAFE_STOP_DICE_COUNT = 2
    }
}
