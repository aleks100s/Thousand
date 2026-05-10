package com.alextos.thousand.presentation.game.tutorial_game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TutorialGameViewModel : ViewModel() {
    private val _state = MutableStateFlow(TutorialGameState())
    val state: StateFlow<TutorialGameState> = _state.asStateFlow()

    fun onAction(action: TutorialGameAction) {
        when (action) {
            TutorialGameAction.Initialize -> Unit
        }
    }
}
