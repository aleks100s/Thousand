package com.alextos.thousand.presentation.other.statistics.turn_statistics

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
    private val _state = MutableStateFlow(_root_ide_package_.com.alextos.thousand.presentation.other.statistics.turn_statistics.TurnsStatisticsState())
    val state: StateFlow<com.alextos.thousand.presentation.other.statistics.turn_statistics.TurnsStatisticsState> = _state.asStateFlow()

    fun onAction(action: com.alextos.thousand.presentation.other.statistics.turn_statistics.TurnsStatisticsAction) {
        when (action) {
            _root_ide_package_.com.alextos.thousand.presentation.other.statistics.turn_statistics.TurnsStatisticsAction.LoadStatistics -> observeStatistics()
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
