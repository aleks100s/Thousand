package com.alextos.thousand.presentation.game.play_game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alextos.thousand.domain.usecase.LoadGameTurnsUseCase
import com.alextos.thousand.domain.usecase.LoadGameUseCase
import com.alextos.thousand.presentation.game.GameRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayGameViewModel(
    savedStateHandle: SavedStateHandle,
    private val loadGameUseCase: LoadGameUseCase,
    private val loadGameTurnsUseCase: LoadGameTurnsUseCase,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<GameRoute.PlayGame>()

    private val _state = MutableStateFlow(PlayGameState())
    val state: StateFlow<PlayGameState> = _state.asStateFlow()

    fun onAction(action: PlayGameAction) {
        when (action) {
            PlayGameAction.LoadGame -> loadGame()
        }
    }

    private fun loadGame() {
        viewModelScope.launch {
            val game = loadGameUseCase(route.gameId)
            val turns = loadGameTurnsUseCase(route.gameId)
            _state.update {
                it.copy(
                    isLoading = false,
                    game = game,
                    turns = turns
                )
            }
        }
    }
}
