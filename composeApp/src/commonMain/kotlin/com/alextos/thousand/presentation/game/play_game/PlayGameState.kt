package com.alextos.thousand.presentation.game.play_game

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.RollAbility
import com.alextos.thousand.domain.models.Turn

data class PlayGameState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val turns: List<Turn> = emptyList(),
    val currentPlayer: Player? = null,
    val currentTurn: List<DiceRoll> = emptyList(),
    val currentRoll: DiceRoll? = null,
    val rollAbility: RollAbility = RollAbility.REQUIRED,
    val isManualInputEnabled: Boolean = false
) {
    val title: String
        get() = game?.let { "Игра №${it.id}" } ?: ""

    fun isEmpty(): Boolean = turns.isEmpty() && currentTurn.isEmpty() && currentRoll == null
}
