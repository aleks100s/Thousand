package com.alextos.thousand.presentation.components

import com.alextos.thousand.domain.usecase.game.server.GameState

val GameState.gameNavigationTitle: String
    get() = when {
        game?.isFinished() == true -> "Игра окончена"
        currentPlayer != null -> "Ходит ${currentPlayer.user.name}"
        else -> "Игра"
    }
