package com.alextos.thousand.presentation.game.game_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.service.StorageService
import com.alextos.thousand.domain.usecase.game.CreateRematchUseCase
import com.alextos.thousand.domain.usecase.game.DeleteGameUseCase
import com.alextos.thousand.domain.usecase.game.GetAllGamesUseCase
import com.alextos.thousand.presentation.models.toUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GamesListViewModel(
    private val getAllGamesUseCase: GetAllGamesUseCase,
    private val deleteGameUseCase: DeleteGameUseCase,
    private val createRematchUseCase: CreateRematchUseCase,
    private val storageService: StorageService,
) : ViewModel() {
    private val _state = MutableStateFlow(GamesListState())
    val state: StateFlow<GamesListState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<GamesListEvent>()
    val events: SharedFlow<GamesListEvent> = _events.asSharedFlow()

    fun onAction(action: GamesListAction) {
        when (action) {
            is GamesListAction.DeleteGame -> deleteGame(action.gameId)
            is GamesListAction.CreateRematch -> createRematch(action.gameId)
            GamesListAction.LoadGames -> observeGames()
            GamesListAction.CompleteFirstLaunch -> completeFirstLaunch()
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
        viewModelScope.launch {
            storageService.isFirstLaunch.collect { isFirstLaunch ->
                _state.update {
                    it.copy(isFirstLaunch = isFirstLaunch)
                }
            }
        }
    }

    private fun deleteGame(gameId: Long) {
        viewModelScope.launch {
            deleteGameUseCase(gameId)
        }
    }

    private fun createRematch(gameId: Long) {
        viewModelScope.launch {
            val game = createRematchUseCase(gameId) ?: return@launch
            _events.emit(GamesListEvent.OpenGame(game.id))
        }
    }

    private fun completeFirstLaunch() {
        viewModelScope.launch {
            storageService.setFirstLaunch(false)
        }
    }
}
