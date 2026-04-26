package com.alextos.thousand.domain.game

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn

class FindCurrentPlayerUseCase {
    operator fun invoke(game: Game?, lastTurn: Turn?): Player? {
        val players = game?.players.orEmpty()
        val index = players.lastIndexOf(lastTurn?.player)
        if (index == -1) {
            return players.firstOrNull()
        }
        return players.getOrNull((index + 1) % players.count())
    }
}