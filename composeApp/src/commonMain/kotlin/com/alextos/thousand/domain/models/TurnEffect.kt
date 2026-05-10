package com.alextos.thousand.domain.models

import com.alextos.thousand.domain.GameConstants.BOLT_FINE
import com.alextos.thousand.domain.GameConstants.OVERTAKE_FINE
import com.alextos.thousand.domain.GameConstants.PIT_SCORE
import com.alextos.thousand.domain.GameConstants.STARTING_LIMIT

data class TurnEffect(
    val id: Long = 0,
    val affectedPlayer: Player,
    val effect: Effect
) {
    fun text(currentPlayer: Player): String {
        return when (effect) {
            Effect.OVERTAKE -> "$currentPlayer обогнал игрока ${affectedPlayer}: -${OVERTAKE_FINE} очков у игрока $affectedPlayer"
            Effect.TRIPLE_BOLT -> "$affectedPlayer три хода подряд получал 0 очков: -${BOLT_FINE} очков"
            Effect.PIT_FALL -> "Счет игрока $affectedPlayer стал $PIT_SCORE и обнулился"
            Effect.BARREL_LIMIT -> "$affectedPlayer не прошел бочку: ход не засчитан"
            Effect.WIN -> "$affectedPlayer победил!"
            Effect.STARTING_LIMIT -> "$affectedPlayer не выбил стартовые $STARTING_LIMIT очков"
        }
    }
}
