package com.alextos.thousand.presentation.menu.game_score

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.usecase.game.FormatTurnEffectUseCase
import com.alextos.thousand.domain.usecase.game.LoadGameUseCase
import com.alextos.thousand.domain.usecase.game.LoadGameTurnsUseCase
import com.alextos.thousand.presentation.menu.MenuRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import com.alextos.thousand.domain.usecase.statistics.TurnsStatisticsUseCase
import com.alextos.thousand.domain.usecase.statistics.RollsStatisticsUseCase
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameScoreViewModel(
    savedStateHandle: SavedStateHandle,
    private val loadGameUseCase: LoadGameUseCase,
    private val loadGameTurnsUseCase: LoadGameTurnsUseCase,
    private val formatTurnEffect: FormatTurnEffectUseCase,
    private val turnsStatisticsUseCase: TurnsStatisticsUseCase,
    private val rollsStatisticsUseCase: RollsStatisticsUseCase,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<MenuRoute.GameScore>()

    private val _state = MutableStateFlow(GameScoreState())
    val state: StateFlow<GameScoreState> = _state.asStateFlow()

    fun onAction(action: GameScoreAction) {
        when (action) {
            GameScoreAction.LoadGame -> loadGame()
        }
    }

    private fun loadGame() {
        viewModelScope.launch {
            val game = loadGameUseCase(route.gameId)
            val turns = loadGameTurnsUseCase(route.gameId)
            val completedGame = game?.takeIf { it.isFinished() }
            val scoreSeries = completedGame?.buildScoreSeries(turns).orEmpty()
            val playerStatistics = completedGame?.let { finishedGame ->
                val globalTurns = turnsStatisticsUseCase().first()
                val globalRolls = rollsStatisticsUseCase().first()
                finishedGame.buildPlayerStatistics(
                    turns,
                    globalTurns.players.associate { player -> player.userId to player.averageTurn },
                    globalRolls.players.associate { player -> player.userId to player.averageRoll },
                )
            }.orEmpty()
            _state.update {
                it.copy(
                    isLoading = false,
                    game = game,
                    scoreSeries = scoreSeries,
                    playerStatistics = playerStatistics,
                    turns = turns.mapIndexed { index, turn -> turn.toHistoryUi(index + 1, formatTurnEffect) },
                )
            }
        }
    }
}
