package com.alextos.thousand.presentation.menu.multiplayer.player_profile

sealed interface PlayerProfileAction {
    data object ShowDeleteAccountDialog : PlayerProfileAction
    data object HideDeleteAccountDialog : PlayerProfileAction
    data object DeleteAccount : PlayerProfileAction
}
