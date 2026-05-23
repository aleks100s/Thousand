package com.alextos.thousand.presentation.game.components

data class GameSettings(
    var isNotificationEnabled: Boolean = true,
    var isVirtualDiceEnabled: Boolean = true,
    var isShakeEnabled: Boolean = true,
    var hasStartLimit: Boolean = true,
    var isBarrel1Active: Boolean = true,
    var isBarrel2Active: Boolean = true,
    var isBarrel3Active: Boolean = false,
    var isTripleBoltFineActive: Boolean = true,
    var isOvertakeFineActive: Boolean = true,
    var host: String? = null
)
