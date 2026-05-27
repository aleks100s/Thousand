package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.GameSettings

object FirebaseGameSettingsMapper {
    fun dictionary(from: GameSettings): Map<String, Any> =
        from.toFirebaseMap()

    fun gameSettings(from: Any?): GameSettings =
        from.asFirebaseMap().toFirebaseGameSettings()
}

internal fun GameSettings.toFirebaseMap(): Map<String, Any> =
    mapOf(
        "isNotificationEnabled" to isNotificationEnabled,
        "isVirtualDiceEnabled" to isVirtualDiceEnabled,
        "isShakeEnabled" to isShakeEnabled,
        "hasStartLimit" to hasStartLimit,
        "isBarrel1Active" to isBarrel1Active,
        "isBarrel2Active" to isBarrel2Active,
        "isBarrel3Active" to isBarrel3Active,
        "isTripleBoltFineActive" to isTripleBoltFineActive,
        "isOvertakeFineActive" to isOvertakeFineActive,
    )

internal fun Map<*, *>?.toFirebaseGameSettings(): GameSettings =
    GameSettings(
        isNotificationEnabled = boolean("isNotificationEnabled", true),
        isVirtualDiceEnabled = boolean("isVirtualDiceEnabled", true),
        isShakeEnabled = boolean("isShakeEnabled", true),
        hasStartLimit = boolean("hasStartLimit", true),
        isBarrel1Active = boolean("isBarrel1Active", true),
        isBarrel2Active = boolean("isBarrel2Active", true),
        isBarrel3Active = boolean("isBarrel3Active", false),
        isTripleBoltFineActive = boolean("isTripleBoltFineActive", true),
        isOvertakeFineActive = boolean("isOvertakeFineActive", true),
    )
