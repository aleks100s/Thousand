package com.alextos.thousand.presentation.game.create_game

sealed interface CreateGameAction {
    data object Initialize : CreateGameAction
    data object ShowAddUserSheet : CreateGameAction
    data object HideAddUserSheet : CreateGameAction
    data class UpdateNewUserName(val value: String) : CreateGameAction
    data object SaveNewUser : CreateGameAction
}
