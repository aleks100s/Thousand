package com.alextos.thousand.domain.models

data class GameSettings(
    var isNotificationEnabled: Boolean = true,
    var isVirtualDiceEnabled: Boolean = true,
    var isShakeEnabled: Boolean = true,
    var hasStartLimit: Boolean = true,
    var isBarrel1Active: Boolean = true,
    var isBarrel2Active: Boolean = true,
    var isBarrel3Active: Boolean = true,
    var isTripleBoltFineActive: Boolean = true,
    var isOvertakeFineActive: Boolean = true,
)