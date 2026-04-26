package com.alextos.thousand.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.usecase.statistics.TurnsStatisticsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TurnsStatisticsViewModel(
    private val turnsStatisticsUseCase: TurnsStatisticsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(TurnsStatisticsState())
    val state: StateFlow<TurnsStatisticsState> = _state.asStateFlow()

    fun onAction(action: TurnsStatisticsAction) {
        when (action) {
            TurnsStatisticsAction.LoadStatistics -> observeStatistics()
        }
    }

    private fun observeStatistics() {
        viewModelScope.launch {
            turnsStatisticsUseCase().collect { statistics ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        totalTurns = statistics.totalTurns,
                        averageTurn = statistics.averageTurn,
                        bestTurn = statistics.bestTurn,
                        players = statistics.players,
                    )
                }
            }
        }
    }
}
