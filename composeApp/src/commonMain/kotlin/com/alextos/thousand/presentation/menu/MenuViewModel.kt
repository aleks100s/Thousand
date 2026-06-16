package com.alextos.thousand.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.service.StorageService
import com.alextos.thousand.domain.usecase.game.crud.GetAllGamesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MenuViewModel(
    private val storageService: StorageService,
    private val getAllGamesUseCase: GetAllGamesUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MenuState())
    val state: StateFlow<MenuState> = _state.asStateFlow()

    init {
        observeFirstLaunch()
        observeLocalGames()
    }

    fun onAction(action: MenuAction) {
        when (action) {
            MenuAction.CompleteFirstLaunch -> completeFirstLaunch()
        }
    }

    private fun observeFirstLaunch() {
        viewModelScope.launch {
            storageService.isFirstLaunch.collect { isFirstLaunch ->
                _state.update {
                    it.copy(isFirstLaunch = isFirstLaunch)
                }
            }
        }
    }

    private fun observeLocalGames() {
        viewModelScope.launch {
            getAllGamesUseCase().collect { games ->
                _state.update {
                    it.copy(hasLocalGames = games.isNotEmpty())
                }
            }
        }
    }

    private fun completeFirstLaunch() {
        viewModelScope.launch {
            storageService.setFirstLaunch(false)
        }
    }
}
