package com.alextos.thousand.presentation.menu.game_score

import com.alextos.thousand.domain.models.User

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn

data class PlayerScoreSeries(
    val userId: String,
    val playerName: String,
    val points: List<Int>,
    val user: User = User(userId, playerName),
)

data class GameResultsPlayerStatistics(
    val userId: String,
    val playerName: String,
    val gameAverageTurn: Double,
    val globalAverageTurn: Double,
    val gameAverageRoll: Double,
    val globalAverageRoll: Double,
    val user: User = User(userId, playerName),
)

internal fun Game.buildScoreSeries(turns: List<Turn>): List<PlayerScoreSeries> {
    val scores = players.associate { it.user.id to 0 }.toMutableMap()
    val points = players.associate { it.user.id to mutableListOf(0) }
    turns.forEach { turn ->
        turn.results.forEach { result -> scores[result.player.user.id] = result.newScore }
        players.forEach { player -> points.getValue(player.user.id).add(scores.getValue(player.user.id)) }
    }
    return players.map { player ->
        PlayerScoreSeries(player.user.id, player.user.name, points.getValue(player.user.id), player.user)
    }
}

internal fun Game.buildPlayerStatistics(
    turns: List<Turn>,
    globalAverageTurns: Map<String, Double>,
    globalAverageRolls: Map<String, Double>,
): List<GameResultsPlayerStatistics> {
    return players.map { player ->
        val playerTurns = turns.filter { turn -> turn.player.sameUserAs(player) }
        val playerRolls = playerTurns.flatMap { turn -> turn.rolls }

        GameResultsPlayerStatistics(
            user = player.user,
            userId = player.user.id,
            playerName = player.user.name,
            gameAverageTurn = playerTurns.averageTotal(),
            globalAverageTurn = globalAverageTurns[player.user.id] ?: 0.0,
            gameAverageRoll = playerRolls.averageResult(),
            globalAverageRoll = globalAverageRolls[player.user.id] ?: 0.0,
        )
    }
}

private fun Player.sameUserAs(other: Player): Boolean {
    return user.id == other.user.id
}

private fun List<Turn>.averageTotal(): Double {
    if (isEmpty()) return 0.0
    return sumOf { turn -> turn.total }.toDouble() / size
}

private fun List<DiceRoll>.averageResult(): Double {
    if (isEmpty()) return 0.0
    return sumOf { roll -> roll.result }.toDouble() / size
}
