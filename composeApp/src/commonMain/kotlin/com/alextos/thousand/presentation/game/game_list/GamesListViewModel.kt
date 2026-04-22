package com.alextos.thousand.presentation.game.game_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.usecase.game.DeleteGameUseCase
import com.alextos.thousand.domain.usecase.game.GetAllGamesUseCase
import com.alextos.thousand.presentation.models.toUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GamesListViewModel(
    private val getAllGamesUseCase: GetAllGamesUseCase,
    private val deleteGameUseCase: DeleteGameUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(GamesListState())
    val state: StateFlow<GamesListState> = _state.asStateFlow()

    fun onAction(action: GamesListAction) {
        when (action) {
            is GamesListAction.DeleteGame -> deleteGame(action.gameId)
            GamesListAction.LoadGames -> observeGames()
        }
    }

    private fun observeGames() {
        viewModelScope.launch {
            getAllGamesUseCase().collect { games ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        games = games.map { game -> game.toUi() },
                    )
                }
            }
        }
    }

    private fun deleteGame(gameId: Long) {
        viewModelScope.launch {
            deleteGameUseCase(gameId)
        }
    }
}
