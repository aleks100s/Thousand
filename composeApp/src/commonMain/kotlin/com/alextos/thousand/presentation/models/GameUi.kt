package com.alextos.thousand.presentation.models

data class GameUi(
    val id: Long,
    val title: String,
    val opponents: String,
    val finishedAt: String?,
    val winnerName: String?,
    val isFinished: Boolean,
    val isVirtualDiceEnabled: Boolean,
    val isNotificationEnabled: Boolean,
)
