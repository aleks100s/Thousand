package com.alextos.thousand.presentation.multiplayer.player_profile

sealed interface PlayerProfileEvent {
    data object GoBack : PlayerProfileEvent
}
