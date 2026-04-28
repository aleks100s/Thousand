package com.alextos.thousand.domain.game.server

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.RollAbility

data class GameState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val currentPlayer: Player? = null,
    val currentTurn: List<DiceRoll> = emptyList(),
    val currentRoll: DiceRoll? = null,
    val rollAbility: RollAbility = RollAbility.REQUIRED,
    val rollBlocked: Boolean = false
)