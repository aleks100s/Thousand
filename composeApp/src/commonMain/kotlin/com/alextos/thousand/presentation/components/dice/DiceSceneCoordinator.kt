package com.alextos.thousand.presentation.components.dice

import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.usecase.game.server.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class DiceSceneRequest(
    val presentationId: String,
    val values: List<Int>,
    val animate: Boolean,
)

sealed interface DiceSceneEvent {
    data object Ready : DiceSceneEvent
    data class RollStarted(val presentationId: String) : DiceSceneEvent
    data class RollFinished(val presentationId: String) : DiceSceneEvent
    data class Error(val presentationId: String?) : DiceSceneEvent
}

enum class DiceScenePhase { Loading, Idle, Animating, ShowingResult, Fallback }

data class DicePresentationState(
    val phase: DiceScenePhase = DiceScenePhase.Loading,
    val request: DiceSceneRequest? = null,
    val ready: Boolean = false,
    val completed: Boolean = true,
    val active: Boolean = true,
) {
    val showResult: Boolean get() = request != null && completed
    val actionsEnabled: Boolean get() = completed
}

/** Owns only presentation state. Game rules and the roll result stay in Kotlin's game server. */
class DiceSceneCoordinator(private val onFinished: () -> Unit) {
    private data class Snapshot(
        val gameId: Long,
        val playerId: String?,
        val history: List<DiceRoll>,
        val roll: DiceRoll?,
    )

    private val _state = MutableStateFlow(DicePresentationState())
    val state = _state.asStateFlow()
    private var snapshot: Snapshot? = null
    private val session = Random.nextLong().toString(16)
    private var sequence = 0L
    private var failed = false

    fun acceptSnapshot(game: GameState, animate: Boolean) {
        val actualGame = game.game ?: return
        val next = Snapshot(actualGame.id, game.currentPlayer?.user?.id, game.currentTurn.toList(), game.currentRoll)
        if (next == snapshot) return
        val initial = snapshot == null
        snapshot = next
        val roll = game.currentRoll
        val request = roll?.let {
            DiceSceneRequest("$session-${++sequence}", it.dice.map { die -> die.value.value }, animate && !initial && _state.value.active)
        }
        _state.value = _state.value.copy(
            request = request,
            completed = request == null,
            phase = when {
                failed -> DiceScenePhase.Fallback
                !_state.value.ready -> DiceScenePhase.Loading
                else -> DiceScenePhase.Idle
            },
        )
        if (failed || !_state.value.active) complete()
    }

    fun onEvent(event: DiceSceneEvent) {
        if (failed) return
        val current = _state.value
        when (event) {
            DiceSceneEvent.Ready -> if (!current.ready) _state.value = current.copy(
                ready = true,
                phase = if (current.showResult) DiceScenePhase.ShowingResult else DiceScenePhase.Idle,
            )
            is DiceSceneEvent.RollStarted -> if (
                event.presentationId == current.request?.presentationId && !current.completed && current.active
            ) {
                _state.value = current.copy(phase = DiceScenePhase.Animating)
            }
            is DiceSceneEvent.RollFinished -> if (event.presentationId == current.request?.presentationId) complete()
            is DiceSceneEvent.Error -> if (event.presentationId == null || event.presentationId == current.request?.presentationId) fail()
        }
    }

    fun fail() {
        failed = true
        _state.value = _state.value.copy(phase = DiceScenePhase.Fallback)
        complete()
    }

    fun setActive(active: Boolean) {
        if (_state.value.active == active) return
        _state.value = _state.value.copy(active = active)
        if (!active) {
            _state.value = _state.value.copy(request = _state.value.request?.copy(animate = false))
            complete()
        }
    }

    private fun complete() {
        val current = _state.value
        if (current.request == null || current.completed) return
        _state.value = current.copy(
            completed = true,
            phase = if (failed) DiceScenePhase.Fallback else DiceScenePhase.ShowingResult,
        )
        onFinished()
    }
}
