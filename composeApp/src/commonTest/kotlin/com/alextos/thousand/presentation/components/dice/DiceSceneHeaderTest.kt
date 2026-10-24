package com.alextos.thousand.presentation.components.dice

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.usecase.game.server.GameState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiceSceneHeaderTest {
    @Test
    fun tutorialHidesTheDecisionPromptButKeepsTheOpponentLabel() {
        val configuration = DiceSceneConfiguration(null, "#000000", DiceSceneBounds(0f, 0f, 1f, 1f), true, isTutorial = true)
        assertEquals("", configuration.promptText)
        assertEquals("ход противника", configuration.copy(isOpponentTurn = true).promptText)
    }

    @Test
    fun snapshotsSavedScoresAndConfiguredBarrelBoundaries() {
        val player = Player(user = User(id = "you", name = "Вы", kind = UserKind.MainUser), currentScore = 420)
        val bot = Player(user = User(id = "bot", name = "Бот", kind = UserKind.Bot), currentScore = 280)
        val roll = DiceRoll(dice = listOf(Die(value = DieValue.FIVE)), result = 50)
        val state = GameState(
            game = Game(id = 1, players = listOf(player, bot), settings = GameSettings(isBarrel2Active = false)),
            currentPlayer = player,
            currentRoll = roll,
            currentTurn = listOf(roll),
        )
        val header = DiceSceneHeader.from(state, true)!!
        assertEquals(listOf(200, 300, 900, 1000), header.barrelBoundaries)
        assertEquals("you", header.activePlayerId)
        assertEquals(420, header.players[0].score)
        assertFalse(header.players[0].bot)
        assertTrue(header.players[1].bot)
        player.currentScore = 470
        assertEquals(420, header.players[0].score, "Previous snapshot must survive mutable Player score updates")
        assertEquals(470, DiceSceneHeader.from(state, true)!!.players[0].score)
    }

    @Test
    fun serializesHeaderAndDecodesInteractionEvents() {
        val player = Player(user = User(id = "p", name = "<Игрок>"), currentScore = 350)
        val header = DiceSceneHeader.from(GameState(game = Game(id = 1, players = listOf(player)), currentPlayer = player), true)
        val configuration = DiceSceneConfiguration(null, "#000000", DiceSceneBounds(0f, 0f, 1f, 1f), true, header = header)
        assertTrue(configuration.script().contains("350"))
        assertEquals(DiceSceneEvent.PlayerSelected("p"), decodeDiceSceneEvent("""{"type":"playerSelected","playerId":"p"}"""))
        assertEquals(DiceSceneEvent.HeaderAnimation(true), decodeDiceSceneEvent("""{"type":"headerAnimation","running":true}"""))
        assertEquals(null, decodeDiceSceneEvent("""{"type":"playerSelected"}"""))
        assertEquals(null, decodeDiceSceneEvent("""{"type":"headerAnimation","running":"invalid"}"""))
    }
}
