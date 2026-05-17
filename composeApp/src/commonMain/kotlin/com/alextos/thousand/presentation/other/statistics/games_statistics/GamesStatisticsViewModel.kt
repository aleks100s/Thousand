package com.alextos.thousand.presentation.other.statistics.games_statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.usecase.statistics.GamesStatisticsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GamesStatisticsViewModel(
    private val gamesStatisticsUseCase: GamesStatisticsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(_root_ide_package_.com.alextos.thousand.presentation.other.statistics.games_statistics.GamesStatisticsState())
    val state: StateFlow<com.alextos.thousand.presentation.other.statistics.games_statistics.GamesStatisticsState> = _state.asStateFlow()

    fun onAction(action: com.alextos.thousand.presentation.other.statistics.games_statistics.GamesStatisticsAction) {
        when (action) {
            _root_ide_package_.com.alextos.thousand.presentation.other.statistics.games_statistics.GamesStatisticsAction.LoadStatistics -> observeStatistics()
        }
    }

    private fun observeStatistics() {
        viewModelScope.launch {
            gamesStatisticsUseCase().collect { statistics ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        finishedGamesCount = statistics.finishedGamesCount,
                        players = statistics.players,
                    )
                }
            }
        }
    }
}
