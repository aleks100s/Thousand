package com.alextos.thousand.presentation.other.statistics.roll_statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.usecase.statistics.RollsStatisticsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RollsStatisticsViewModel(
    private val rollsStatisticsUseCase: RollsStatisticsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(_root_ide_package_.com.alextos.thousand.presentation.other.statistics.roll_statistics.RollsStatisticsState())
    val state: StateFlow<com.alextos.thousand.presentation.other.statistics.roll_statistics.RollsStatisticsState> = _state.asStateFlow()

    fun onAction(action: com.alextos.thousand.presentation.other.statistics.roll_statistics.RollsStatisticsAction) {
        when (action) {
            _root_ide_package_.com.alextos.thousand.presentation.other.statistics.roll_statistics.RollsStatisticsAction.LoadStatistics -> observeStatistics()
        }
    }

    private fun observeStatistics() {
        viewModelScope.launch {
            rollsStatisticsUseCase().collect { statistics ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        totalRolls = statistics.totalRolls,
                        averageRoll = statistics.averageRoll,
                        bestRoll = statistics.bestRoll,
                        averageRollChain = statistics.averageRollChain,
                        bestRollChain = statistics.bestRollChain,
                        players = statistics.players,
                    )
                }
            }
        }
    }
}
