package com.alextos.thousand.domain.game

import com.alextos.thousand.domain.GameConstants.BOLT_FINE
import com.alextos.thousand.domain.GameConstants.OVERTAKE_FINE
import com.alextos.thousand.domain.GameConstants.PIT_SCORE
import com.alextos.thousand.domain.GameConstants.STARTING_LIMIT
import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.TurnEffect

class FormatTurnEffectUseCase {
    operator fun invoke(
        effect: TurnEffect,
        currentPlayer: Player,
        isTutorial: Boolean = false,
    ): String {
        return if (isTutorial) {
            formatTutorialEffect(effect, currentPlayer)
        } else {
            formatEffect(effect, currentPlayer)
        }
    }

    private fun formatEffect(effect: TurnEffect, currentPlayer: Player): String {
        return when (effect.effect) {
            Effect.OVERTAKE -> "$currentPlayer обогнал игрока ${effect.affectedPlayer}: -${OVERTAKE_FINE} очков у игрока ${effect.affectedPlayer}"
            Effect.TRIPLE_BOLT -> "${effect.affectedPlayer} три хода подряд получал 0 очков: -${BOLT_FINE} очков"
            Effect.PIT_FALL -> "Счет игрока ${effect.affectedPlayer} стал $PIT_SCORE и обнулился"
            Effect.BARREL_LIMIT -> "${effect.affectedPlayer} не прошел бочку: ход не засчитан"
            Effect.WIN -> "${effect.affectedPlayer} победил!"
            Effect.STARTING_LIMIT -> "${effect.affectedPlayer} не выбил стартовые $STARTING_LIMIT очков"
        }
    }

    private fun formatTutorialEffect(effect: TurnEffect, currentPlayer: Player): String {
        return when (effect.effect) {
            Effect.OVERTAKE -> "$currentPlayer обогнал игрока ${effect.affectedPlayer}. По правилу обгона игрок, которого обогнали, теряет $OVERTAKE_FINE очков."
            Effect.TRIPLE_BOLT -> "${effect.affectedPlayer} получил третий болт подряд. За три нулевых хода подряд действует штраф: -$BOLT_FINE очков."
            Effect.PIT_FALL -> "${effect.affectedPlayer} набрал ровно $PIT_SCORE очков. Это яма: счет игрока обнуляется."
            Effect.BARREL_LIMIT -> "${effect.affectedPlayer} остался внутри бочки. Такой ход не засчитывается: нужно набрать достаточно очков, чтобы выйти за верхнюю границу бочки."
            Effect.WIN -> "${effect.affectedPlayer} достиг цели игры и победил. Учебная партия закончена."
            Effect.STARTING_LIMIT -> "${effect.affectedPlayer} пока не прошел стартовый лимит. Нужно набрать минимум $STARTING_LIMIT очков за ход, иначе очки не записываются."
        }
    }
}
