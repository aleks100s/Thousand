package com.alextos.thousand.presentation.game.create_game

import com.alextos.thousand.domain.models.User

sealed interface CreateGameAction {
    data object Initialize : CreateGameAction
    data object ShowAddUserSheet : CreateGameAction
    data object ShowAddBotSheet : CreateGameAction
    data object HideAddUserSheet : CreateGameAction
    data object OpenPlayersStep : CreateGameAction
    data object OpenSettingsStep : CreateGameAction
    data class UpdateNewUserName(val value: String) : CreateGameAction
    data class ToggleUserSelection(val user: User) : CreateGameAction
    data class DeleteUser(val user: User) : CreateGameAction
    data class SetNotificationEnabled(val isEnabled: Boolean) : CreateGameAction
    data class SetVirtualDiceEnabled(val isEnabled: Boolean) : CreateGameAction
    data class SetShakeEnabled(val isEnabled: Boolean) : CreateGameAction
    data class SetHasStartLimit(val isEnabled: Boolean) : CreateGameAction
    data class SetBarrel1Active(val isEnabled: Boolean) : CreateGameAction
    data class SetBarrel2Active(val isEnabled: Boolean) : CreateGameAction
    data class SetBarrel3Active(val isEnabled: Boolean) : CreateGameAction
    data class SetTripleBoltFineActive(val isEnabled: Boolean) : CreateGameAction
    data class SetOvertakeFineActive(val isEnabled: Boolean) : CreateGameAction
    data object SaveNewUser : CreateGameAction
    data object CreateGame: CreateGameAction
}
