package com.alextos.thousand.presentation.menu.multiplayer

sealed interface MultiplayerAction {
    data class OpenInvitation(val code: String, val id: Long) : MultiplayerAction
    data object ShowJoinLobbySheet :
        MultiplayerAction
    data object HideJoinLobbySheet :
        MultiplayerAction
    data class UpdateLobbyId(val value: String) :
        MultiplayerAction
    data object RetryConnection : MultiplayerAction
    data object JoinLobby : MultiplayerAction
    data class DeleteGame(val key: String) :
        MultiplayerAction
    data class DisconnectFromLobby(val key: String) :
        MultiplayerAction
}
