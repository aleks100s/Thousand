package com.alextos.thousand.presentation.menu.game_score

/** Flat UI rows are prepared with screen state, never rebuilt during composition. */
sealed interface TurnHistoryRow {
    val key: String

    data class Header(val turn: TurnHistoryUi) : TurnHistoryRow {
        override val key = "turn-${turn.id}-header"
    }
    data class Roll(val turnId: Long, val number: Int, val roll: HistoryRollUi) : TurnHistoryRow {
        override val key = "turn-$turnId-roll-$number"
    }
    data class Effect(val turnId: Long, val index: Int, val message: String) : TurnHistoryRow {
        override val key = "turn-$turnId-effect-$index"
    }
    data class Footer(val turn: TurnHistoryUi) : TurnHistoryRow {
        override val key = "turn-${turn.id}-footer"
    }
}

internal fun List<TurnHistoryUi>.toHistoryRows(): List<TurnHistoryRow> = buildList {
    this@toHistoryRows.forEach { turn ->
        add(TurnHistoryRow.Header(turn))
        turn.rolls.forEachIndexed { index, roll -> add(TurnHistoryRow.Roll(turn.id, index + 1, roll)) }
        turn.effects.forEachIndexed { index, message -> add(TurnHistoryRow.Effect(turn.id, index, message)) }
        add(TurnHistoryRow.Footer(turn))
    }
}
