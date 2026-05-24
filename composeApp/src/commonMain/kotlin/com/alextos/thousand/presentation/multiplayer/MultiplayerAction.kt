package com.alextos.thousand.presentation.multiplayer

sealed interface MultiplayerAction {
    data object ShowLoginSheet : MultiplayerAction
    data object HideLoginSheet : MultiplayerAction
    data object ShowLogoutSheet : MultiplayerAction
    data object HideLogoutSheet : MultiplayerAction
    data object ShowJoinLobbySheet : MultiplayerAction
    data object HideJoinLobbySheet : MultiplayerAction
    data class UpdateEmail(val value: String) : MultiplayerAction
    data class UpdatePassword(val value: String) : MultiplayerAction
    data class UpdateLobbyId(val value: String) : MultiplayerAction
    data object LogIn : MultiplayerAction
    data object SignUp : MultiplayerAction
    data object JoinLobby : MultiplayerAction
    data object SignOut : MultiplayerAction
}
