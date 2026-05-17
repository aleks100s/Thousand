package com.alextos.thousand.presentation.other.statistics.events_statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.usecase.statistics.EventsStatisticsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventsStatisticsViewModel(
    private val eventsStatisticsUseCase: EventsStatisticsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(EventsStatisticsState())
    val state: StateFlow<EventsStatisticsState> = _state.asStateFlow()

    fun onAction(action: EventsStatisticsAction) {
        when (action) {
            EventsStatisticsAction.LoadStatistics -> observeStatistics()
        }
    }

    private fun observeStatistics() {
        viewModelScope.launch {
            eventsStatisticsUseCase().collect { statistics ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        pitFalls = statistics.pitFalls,
                        overtakes = statistics.overtakes,
                        tripleBolts = statistics.tripleBolts,
                        players = statistics.players,
                    )
                }
            }
        }
    }
}
