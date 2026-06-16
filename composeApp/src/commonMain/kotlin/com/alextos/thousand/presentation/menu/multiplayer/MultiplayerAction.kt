package com.alextos.thousand.presentation.menu.multiplayer

sealed interface MultiplayerAction {
    data object ShowLoginSheet :
        MultiplayerAction
    data object HideLoginSheet :
        MultiplayerAction
    data object ShowJoinLobbySheet :
        MultiplayerAction
    data object HideJoinLobbySheet :
        MultiplayerAction
    data class UpdateLobbyId(val value: String) :
        MultiplayerAction
    data class LogIn(
        val email: String,
        val password: String,
    ) : MultiplayerAction
    data class SignUp(
        val email: String,
        val password: String,
    ) : MultiplayerAction
    data object JoinLobby : MultiplayerAction
    data class DeleteGame(val key: String) :
        MultiplayerAction
    data class DisconnectFromLobby(val key: String) :
        MultiplayerAction
}
