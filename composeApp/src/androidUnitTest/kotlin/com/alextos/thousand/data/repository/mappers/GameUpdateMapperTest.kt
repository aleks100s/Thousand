package com.alextos.thousand.data.repository.mappers

import com.alextos.thousand.data.mappers.firebase.FirebaseGameMapper
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.RemoteGame
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameUpdateMapperTest {
    @Test
    fun finishingTheTurnDeletesTheStoredRollInAPartialUpdate() {
        val roll = DiceRoll(dice = listOf(Die(value = DieValue.ONE)), result = 10)
        val game = RemoteGame(players = emptyList(), host = "host", key = "game", currentTurn = listOf(roll), currentRoll = roll)
        val stored = game.toDatabaseMap().toMutableMap()
        val update = game.copy(currentTurn = emptyList(), currentRoll = null, currentPlayerIndex = 1).toDatabaseUpdateMap()
        assertTrue(update.containsKey("currentRoll"))
        assertNull(update["currentRoll"])
        assertFalse(update.containsKey("onlinePlayerIds"))
        // Realtime Database treats null as deletion and absent keys as unchanged.
        update.forEach { (key, value) -> if (value == null) stored.remove(key) else stored[key] = value }
        val received = FirebaseGameMapper.game(stored, "game")!!
        assertNull(received.currentRoll)
        assertTrue(received.currentTurn.isEmpty())
        assertEquals(1, received.currentPlayerIndex)
    }

    @Test
    fun aNewRollIsIncludedInTheUpdate() {
        val roll = DiceRoll(dice = listOf(Die(value = DieValue.FIVE)), result = 5)
        val game = RemoteGame(players = emptyList(), host = "host", key = "game", currentTurn = listOf(roll), currentRoll = roll)
        val received = FirebaseGameMapper.game(game.toDatabaseUpdateMap(), "game")!!
        assertEquals(roll, received.currentRoll)
        assertEquals(listOf(roll), received.currentTurn)
    }
}
