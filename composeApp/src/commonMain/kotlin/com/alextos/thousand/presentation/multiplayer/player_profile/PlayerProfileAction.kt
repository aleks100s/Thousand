package com.alextos.thousand.presentation.multiplayer.player_profile

sealed interface PlayerProfileAction {
    data object ShowLogoutDialog : PlayerProfileAction
    data object HideLogoutDialog : PlayerProfileAction
    data object ShowDeleteAccountDialog : PlayerProfileAction
    data object HideDeleteAccountDialog : PlayerProfileAction
    data object SignOut : PlayerProfileAction
    data object DeleteAccount : PlayerProfileAction
}
