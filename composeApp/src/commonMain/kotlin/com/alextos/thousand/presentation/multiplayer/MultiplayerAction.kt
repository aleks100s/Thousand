package com.alextos.thousand.presentation.multiplayer

sealed interface MultiplayerAction {
    data object ShowLoginSheet : MultiplayerAction
    data object HideLoginSheet : MultiplayerAction
    data class UpdateEmail(val value: String) : MultiplayerAction
    data class UpdatePassword(val value: String) : MultiplayerAction
    data object LogIn : MultiplayerAction
}
