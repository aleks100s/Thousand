package com.alextos.thousand.presentation.menu.game_score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TurnHistoryRowsTest {
    @Test
    fun longTurnsProduceIndependentRowsWithUniqueKeysEvenForIdenticalRolls() {
        val roll = HistoryRollUi(emptyList(), 5)
        val turn = TurnHistoryUi(10, 1, "Игрок", List(1000) { roll }, listOf("Сообщение"), 5000, 5000, emptyList())
        val rows = listOf(turn, turn.copy(id = 11, number = 2)).toHistoryRows()
        assertEquals(2006, rows.size)
        assertEquals(rows.size, rows.map { it.key }.toSet().size)
        assertIs<TurnHistoryRow.Header>(rows.first())
        assertEquals(1000, assertIs<TurnHistoryRow.Roll>(rows[1000]).number)
        assertIs<TurnHistoryRow.Effect>(rows[1001])
        assertIs<TurnHistoryRow.Footer>(rows[1002])
        assertIs<TurnHistoryRow.Header>(rows[1003])
        assertIs<TurnHistoryRow.Footer>(rows.last())
    }
}
