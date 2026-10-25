package com.alextos.thousand.presentation.components

data class GameMessageBubble(
    val id: Long,
    val text: String,
    val isReply: Boolean = false,
    val isReaction: Boolean = false,
)
