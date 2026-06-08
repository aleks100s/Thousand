package com.alextos.thousand.presentation.menu.multiplayer.player_profile

sealed interface PlayerProfileEvent {
    data object GoBack : PlayerProfileEvent
}
