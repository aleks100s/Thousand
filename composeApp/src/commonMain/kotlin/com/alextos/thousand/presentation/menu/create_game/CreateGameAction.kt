package com.alextos.thousand.presentation.menu.create_game

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.GameSettings

sealed interface CreateGameAction {
    data object Initialize : CreateGameAction
    data object TogglePlayersEditing : CreateGameAction
    data object FinishPlayersEditing : CreateGameAction
    data object ShowAddUserSheet : CreateGameAction
    data object ShowAddBotSheet : CreateGameAction
    data object HidePlayerEditor : CreateGameAction
    data class EditPlayer(val user: User) : CreateGameAction
    data class UpdatePlayerName(val value: String) : CreateGameAction
    data object SavePlayer : CreateGameAction
    data class RequestDeletePlayer(val user: User) : CreateGameAction
    data object CancelDeletePlayer : CreateGameAction
    data object ConfirmDeletePlayer : CreateGameAction
    data object OpenPlayersStep : CreateGameAction
    data object OpenSettingsStep : CreateGameAction
    data class ToggleUserSelection(val user: User) : CreateGameAction
    data class UpdateGameSettings(val settings: GameSettings) : CreateGameAction
    data object CreateGame : CreateGameAction
}
