package com.alextos.thousand.presentation.game.game_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.usecase.GetAllGamesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GamesListViewModel(
    private val getAllGamesUseCase: GetAllGamesUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(GamesListState())
    val state: StateFlow<GamesListState> = _state.asStateFlow()

    fun onAction(action: GamesListAction) {
        when (action) {
            GamesListAction.LoadGames -> observeGames()
        }
    }

    private fun observeGames() {
        viewModelScope.launch {
            getAllGamesUseCase().collect { games ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        games = games,
                    )
                }
            }
        }
    }
}
