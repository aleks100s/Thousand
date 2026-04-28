package com.alextos.thousand.presentation.statistics.dice_statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.usecase.statistics.DiceStatisticsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiceStatisticsViewModel(
    private val diceStatisticsUseCase: DiceStatisticsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(DiceStatisticsState())
    val state: StateFlow<DiceStatisticsState> = _state.asStateFlow()

    fun onAction(action: DiceStatisticsAction) {
        when (action) {
            DiceStatisticsAction.LoadStatistics -> observeStatistics()
        }
    }

    private fun observeStatistics() {
        viewModelScope.launch {
            diceStatisticsUseCase().collect { statistics ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        totalDice = statistics.totalDice,
                        averageDie = statistics.averageDie,
                        distribution = statistics.distribution,
                        players = statistics.players,
                    )
                }
            }
        }
    }
}
