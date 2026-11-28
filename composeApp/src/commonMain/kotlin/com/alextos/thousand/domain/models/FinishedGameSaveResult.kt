package com.alextos.thousand.domain.models

data class FinishedGameSaveResult(
    val game: RemoteGame,
    val newlyFinished: Boolean,
)
