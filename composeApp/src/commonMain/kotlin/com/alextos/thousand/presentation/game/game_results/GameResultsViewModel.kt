package com.alextos.thousand.presentation.game.game_results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.usecase.game.LoadGameTurnsUseCase
import com.alextos.thousand.domain.usecase.game.LoadGameUseCase
import com.alextos.thousand.domain.usecase.statistics.RollsStatisticsUseCase
import com.alextos.thousand.domain.usecase.statistics.TurnsStatisticsUseCase
import com.alextos.thousand.presentation.game.GameRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameResultsViewModel(
    savedStateHandle: SavedStateHandle,
    private val loadGameUseCase: LoadGameUseCase,
    private val loadGameTurnsUseCase: LoadGameTurnsUseCase,
    private val turnsStatisticsUseCase: TurnsStatisticsUseCase,
    private val rollsStatisticsUseCase: RollsStatisticsUseCase,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<GameRoute.GameResults>()

    private val _state = MutableStateFlow(GameResultsState())
    val state: StateFlow<GameResultsState> = _state.asStateFlow()

    fun onAction(action: GameResultsAction) {
        when (action) {
            GameResultsAction.LoadResults -> loadResults()
        }
    }

    private fun loadResults() {
        viewModelScope.launch {
            val game = loadGameUseCase(route.gameId)
            val turns = loadGameTurnsUseCase(route.gameId)
            val globalTurnStatistics = turnsStatisticsUseCase().first()
            val globalRollStatistics = rollsStatisticsUseCase().first()

            _state.update {
                it.copy(
                    isLoading = false,
                    game = game,
                    scoreSeries = game?.buildScoreSeries(turns).orEmpty(),
                    playerStatistics = game?.buildPlayerStatistics(
                        turns = turns,
                        globalAverageTurns = globalTurnStatistics.players.associateBy(
                            keySelector = { player -> player.userId },
                            valueTransform = { player -> player.averageTurn },
                        ),
                        globalAverageRolls = globalRollStatistics.players.associateBy(
                            keySelector = { player -> player.userId },
                            valueTransform = { player -> player.averageRoll },
                        ),
                    ).orEmpty(),
                )
            }
        }
    }

    private fun Game.buildScoreSeries(turns: List<Turn>): List<PlayerScoreSeries> {
        val scores = players.associateWith { 0 }.toMutableMap()
        val points = players.associateWith { mutableListOf(0) }

        turns.forEach { turn ->
            turn.results.forEach { result ->
                scores[result.player] = result.newScore
            }

            players.forEach { player ->
                points.getValue(player).add(scores.getValue(player))
            }
        }

        return players.map { player ->
            PlayerScoreSeries(
                userId = player.user.id,
                playerName = player.user.name,
                points = points.getValue(player),
            )
        }
    }

    private fun Game.buildPlayerStatistics(
        turns: List<Turn>,
        globalAverageTurns: Map<Long, Double>,
        globalAverageRolls: Map<Long, Double>,
    ): List<GameResultsPlayerStatistics> {
        return players.map { player ->
            val playerTurns = turns.filter { turn -> turn.player.sameUserAs(player) }
            val playerRolls = playerTurns.flatMap { turn -> turn.rolls }

            GameResultsPlayerStatistics(
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

    private fun List<com.alextos.thousand.domain.models.DiceRoll>.averageResult(): Double {
        if (isEmpty()) return 0.0
        return sumOf { roll -> roll.result }.toDouble() / size
    }
}
