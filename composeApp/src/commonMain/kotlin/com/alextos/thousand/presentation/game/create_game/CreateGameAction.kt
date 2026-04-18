package com.alextos.thousand.presentation.game.create_game

import com.alextos.thousand.domain.models.User

sealed interface CreateGameAction {
    data object Initialize : CreateGameAction
    data object ShowAddUserSheet : CreateGameAction
    data object HideAddUserSheet : CreateGameAction
    data object ConsumeCreatedGame : CreateGameAction
    data class UpdateNewUserName(val value: String) : CreateGameAction
    data class ToggleUserSelection(val user: User) : CreateGameAction
    data object SaveNewUser : CreateGameAction
    data object CreateGame: CreateGameAction
}
