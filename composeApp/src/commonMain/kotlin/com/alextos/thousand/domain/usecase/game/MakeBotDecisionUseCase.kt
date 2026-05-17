package com.alextos.thousand.domain.usecase.game

import com.alextos.thousand.domain.GameConstants.BARREL_1
import com.alextos.thousand.domain.GameConstants.BARREL_2
import com.alextos.thousand.domain.GameConstants.BARREL_3
import com.alextos.thousand.domain.GameConstants.GAME_GOAL
import com.alextos.thousand.domain.GameConstants.PIT_SCORE
import com.alextos.thousand.domain.GameConstants.STARTING_LIMIT
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.RollAbility

class MakeBotDecisionUseCase {
    operator fun invoke(
        rollAbility: RollAbility,
        bot: Player,
        game: Game,
        turnTotal: Int
    ): BotDecision {
        when (rollAbility) {
            RollAbility.UNAVAILABLE -> return BotDecision.FINISH
            RollAbility.REQUIRED -> return BotDecision.CONTINUE
            else -> {
                if (game.hasStartLimit && bot.hasPassedStartLimit.not() && turnTotal < STARTING_LIMIT) return BotDecision.CONTINUE
                if (game.hasStartLimit && bot.hasPassedStartLimit.not() && turnTotal >= STARTING_LIMIT) return BotDecision.FINISH
                if (bot.currentScore + turnTotal >= GAME_GOAL) return BotDecision.FINISH
                if (bot.currentScore + turnTotal == PIT_SCORE) return BotDecision.CONTINUE
                if (bot.isInBarrel(game, turnTotal)) return BotDecision.CONTINUE
                if (bot.boltCount == DANGEROUS_BOLT_COUNT) return BotDecision.FINISH
                if (rollAbility.count <= SAFE_STOP_DICE_COUNT) return BotDecision.FINISH
                if (turnTotal >= 50) return BotDecision.FINISH

                return BotDecision.CONTINUE
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

enum class BotDecision {
    FINISH,
    CONTINUE
}