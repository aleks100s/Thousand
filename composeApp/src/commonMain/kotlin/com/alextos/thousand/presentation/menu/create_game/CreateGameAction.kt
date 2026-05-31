package com.alextos.thousand.presentation.menu.create_game

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.GameSettings

sealed interface CreateGameAction {
    data object Initialize : CreateGameAction
    data object ShowAddUserSheet : CreateGameAction
    data object ShowAddBotSheet : CreateGameAction
    data object HideAddUserSheet : CreateGameAction
    data object OpenPlayersStep : CreateGameAction
    data object OpenSettingsStep : CreateGameAction
    data class UpdateNewUserName(val value: String) : CreateGameAction
    data class ToggleUserSelection(val user: User) : CreateGameAction
    data class UpdateGameSettings(val settings: GameSettings) : CreateGameAction
    data object SaveNewUser : CreateGameAction
    data object CreateGame: CreateGameAction
}
