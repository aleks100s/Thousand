package com.alextos.thousand.presentation.menu.game_score

import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.usecase.game.FormatTurnEffectUseCase

data class HistoryDieUi(val value: DieValue, val scoring: Boolean)
data class HistoryRollUi(val dice: List<HistoryDieUi>, val score: Int)
data class HistoryScoreChangeUi(val name: String, val change: Int, val score: Int)
data class TurnHistoryUi(
    val id: Long,
    val number: Int,
    val playerName: String,
    val rolls: List<HistoryRollUi>,
    val effects: List<String>,
    val total: Int,
    val playerScore: Int?,
    val otherChanges: List<HistoryScoreChangeUi>,
)

fun Turn.toHistoryUi(number: Int, formatEffect: FormatTurnEffectUseCase): TurnHistoryUi = TurnHistoryUi(
    id = id,
    number = number,
    playerName = player.user.name,
    rolls = rolls.map { roll ->
        val counts = roll.dice.groupingBy { it.value }.eachCount()
        HistoryRollUi(
            dice = roll.dice.map { die ->
                HistoryDieUi(die.value, die.value == DieValue.ONE || die.value == DieValue.FIVE || counts.getValue(die.value) >= 3)
            },
            score = roll.result,
        )
    },
    effects = effects.map { formatEffect(it, player) },
    total = total,
    playerScore = results.firstOrNull { it.player.user.id == player.user.id }?.newScore,
    otherChanges = results.filter { it.player.user.id != player.user.id }.map {
        HistoryScoreChangeUi(it.player.user.name, it.scoreChange, it.newScore)
    },
)
