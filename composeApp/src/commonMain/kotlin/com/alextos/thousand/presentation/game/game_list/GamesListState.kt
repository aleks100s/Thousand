package com.alextos.thousand.presentation.game.game_list

import com.alextos.thousand.presentation.models.GameUi

data class GamesListState(
    val isLoading: Boolean = true,
    val isFirstLaunch: Boolean = true,
    val games: List<GameUi> = emptyList(),
) {
    val isFABShown: Boolean = isLoading.not() && games.isNotEmpty()
}
