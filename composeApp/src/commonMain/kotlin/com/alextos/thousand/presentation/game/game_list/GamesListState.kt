package com.alextos.thousand.presentation.game.game_list

import com.alextos.thousand.domain.models.Game

data class GamesListState(
    val isLoading: Boolean = true,
    val games: List<Game> = emptyList(),
)
