package com.alextos.thousand.presentation.components.dice

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.usecase.game.server.GameState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DiceSceneCoordinatorTest {
    @Test
    fun endingTheTurnClearsTheRollAndIgnoresItsCallbacks() {
        var finishes = 0
        val coordinator = DiceSceneCoordinator { finishes++ }
        val previous = game(listOf(roll))
        coordinator.acceptSnapshot(previous, true)
        coordinator.onEvent(DiceSceneEvent.Ready)
        val oldId = coordinator.state.value.request!!.presentationId
        coordinator.onEvent(DiceSceneEvent.RollFinished(oldId))
        coordinator.clearCompletedRoll()
        coordinator.acceptSnapshot(previous, true)
        coordinator.onEvent(DiceSceneEvent.RollStarted(oldId))
        coordinator.onEvent(DiceSceneEvent.RollFinished(oldId))
        assertEquals(null, coordinator.state.value.request)
        assertFalse(coordinator.state.value.showResult)
        assertTrue(coordinator.state.value.actionsEnabled)
        assertEquals(1, finishes)
        coordinator.acceptSnapshot(game(player = "opponent"), true)
        assertEquals(null, coordinator.state.value.request)
        assertEquals(1, finishes)
        coordinator.acceptSnapshot(game(listOf(roll), player = "opponent"), true)
        assertNotEquals(oldId, coordinator.state.value.request!!.presentationId)
        assertTrue(coordinator.state.value.request!!.animate)
    }

    @Test
    fun opponentPromptIsPassedToTheScene() {
        val configuration = DiceSceneConfiguration(null, "#000000", DiceSceneBounds(0f, 0f, 1f, 1f), true)
        assertEquals("продолжить или сохранить?", configuration.promptText)
        val opponent = configuration.copy(isOpponentTurn = true)
        assertEquals("ход противника", opponent.promptText)
        assertTrue(opponent.script().contains("ход противника"))
    }

    @Test
    fun presentsTheKotlinRollScore() {
        for ((value, label) in mapOf(0 to "0 очков", 5 to "5 очков", 15 to "15 очков", 100 to "100 очков")) {
            assertEquals(label, DiceSceneRequest("score", listOf(1), false, score = value).scoreText)
        }
        val coordinator = DiceSceneCoordinator {}
        coordinator.acceptSnapshot(game(listOf(roll.copy(result = 15))), false)
        assertEquals("15 очков", coordinator.state.value.request?.scoreText)
        assertTrue(DiceSceneConfiguration(
            coordinator.state.value.request, "#000000", DiceSceneBounds(0f, 0f, 1f, 1f), true,
        ).script().contains("15 очков"))
    }

    @Test
    fun highlightsOnesFivesAndGroupsOfAtLeastThree() {
        fun indices(vararg values: Int) = DiceSceneRequest("test", values.toList(), false).scoringIndices
        assertEquals(listOf(0, 2, 4), indices(1, 2, 5, 2, 1))
        for (face in listOf(2, 3, 4, 6)) {
            assertEquals(emptyList(), indices(face, face))
            assertEquals(listOf(0, 1, 2), indices(face, face, face))
            assertEquals(listOf(0, 1, 2, 3, 4), indices(face, face, face, face, face))
        }
        assertEquals(emptyList(), indices(2, 3, 4, 6))
        assertEquals(emptyList(), indices())
    }

    private val roll = DiceRoll(dice = listOf(Die(value = DieValue.entries.first())), result = 100)
    private fun game(history: List<DiceRoll> = emptyList(), player: String = "player") = GameState(
        game = Game(id = 1, players = emptyList()), currentPlayer = Player(user = User(id = player, name = player)),
        currentTurn = history, currentRoll = history.lastOrNull(),
    )

    @Test
    fun initialSnapshotIsStaticAndOnlyNewRollAnimates() {
        val coordinator = DiceSceneCoordinator {}
        coordinator.acceptSnapshot(game(listOf(roll)), true)
        assertFalse(coordinator.state.value.request!!.animate)
        val first = coordinator.state.value.request!!.presentationId
        coordinator.acceptSnapshot(game(listOf(roll, roll)), true)
        assertTrue(coordinator.state.value.request!!.animate)
        assertNotEquals(first, coordinator.state.value.request!!.presentationId)
    }

    @Test
    fun repeatedSnapshotsAndReactionsDoNotRestartPresentation() {
        val coordinator = DiceSceneCoordinator {}
        coordinator.acceptSnapshot(game(), true)
        coordinator.acceptSnapshot(game(listOf(roll)), true)
        val request = coordinator.state.value.request
        coordinator.acceptSnapshot(game(listOf(roll)).copy(onlinePlayerIds = setOf("other")), true)
        assertEquals(request, coordinator.state.value.request)
    }

    @Test
    fun staleAndRepeatedCallbacksCannotUnlockAnotherRoll() {
        var completions = 0
        val coordinator = DiceSceneCoordinator { completions++ }
        coordinator.acceptSnapshot(game(), true)
        coordinator.acceptSnapshot(game(listOf(roll)), true)
        val oldId = coordinator.state.value.request!!.presentationId
        coordinator.acceptSnapshot(game(listOf(roll, roll)), true)
        coordinator.onEvent(DiceSceneEvent.RollFinished(oldId))
        coordinator.onEvent(DiceSceneEvent.Error(oldId))
        assertFalse(coordinator.state.value.actionsEnabled)
        val id = coordinator.state.value.request!!.presentationId
        coordinator.onEvent(DiceSceneEvent.RollFinished(id))
        coordinator.onEvent(DiceSceneEvent.RollFinished(id))
        assertEquals(1, completions)
        assertTrue(coordinator.state.value.actionsEnabled)
    }

    @Test
    fun timeoutFallsBackAndFutureRollsCompleteWithoutWebView() {
        var completions = 0
        val coordinator = DiceSceneCoordinator { completions++ }
        coordinator.acceptSnapshot(game(), true)
        coordinator.acceptSnapshot(game(listOf(roll)), true)
        coordinator.fail()
        coordinator.fail()
        assertEquals(1, completions)
        assertEquals(DiceScenePhase.Fallback, coordinator.state.value.phase)
        coordinator.onEvent(DiceSceneEvent.Ready)
        coordinator.acceptSnapshot(game(listOf(roll, roll)), true)
        assertEquals(2, completions)
        assertTrue(coordinator.state.value.showResult)
        assertEquals(DiceScenePhase.Fallback, coordinator.state.value.phase)
    }

    @Test
    fun backgroundCompletesOnceAndResumeDoesNotReplay() {
        var completions = 0
        val coordinator = DiceSceneCoordinator { completions++ }
        coordinator.acceptSnapshot(game(), true)
        coordinator.acceptSnapshot(game(listOf(roll)), true)
        coordinator.setActive(false)
        coordinator.setActive(true)
        assertEquals(1, completions)
        assertFalse(coordinator.state.value.request!!.animate)
        assertTrue(coordinator.state.value.showResult)
    }

    @Test
    fun noRollNeverSendsFinishAndPlayerChangeGetsNewId() {
        var completions = 0
        val coordinator = DiceSceneCoordinator { completions++ }
        coordinator.acceptSnapshot(game(), true)
        coordinator.onEvent(DiceSceneEvent.Ready)
        coordinator.setActive(false)
        assertEquals(0, completions)
        coordinator.setActive(true)
        coordinator.acceptSnapshot(game(listOf(roll)), false)
        val first = coordinator.state.value.request!!.presentationId
        coordinator.acceptSnapshot(game(listOf(roll), "other"), false)
        assertNotEquals(first, coordinator.state.value.request!!.presentationId)
        assertFalse(coordinator.state.value.request!!.animate)
    }

    @Test
    fun bridgeRejectsMalformedMessagesAndSafelyQuotesCommands() {
        assertEquals(null, decodeDiceSceneEvent("not json"))
        assertEquals(null, decodeDiceSceneEvent("{\"type\":\"rollFinished\"}"))
        assertEquals(DiceSceneEvent.Ready, decodeDiceSceneEvent("{\"type\":\"ready\"}"))
        val configuration = DiceSceneConfiguration(
            DiceSceneRequest("quote'\"\\\n", listOf(1), false), "#000000",
            DiceSceneBounds(0f, 0f, 1f, 1f), true,
        )
        assertTrue(configuration.script().startsWith("window.DiceScene && window.DiceScene.receive(\""))
        assertFalse(configuration.script().contains('\n'))
    }
}
