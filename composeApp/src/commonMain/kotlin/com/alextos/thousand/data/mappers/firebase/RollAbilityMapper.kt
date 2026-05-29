package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.RollAbility

internal fun Any?.toFirebaseRollAbility(): RollAbility {
    val name = this as? String
    if (name != null) {
        return RollAbility.entries.firstOrNull { ability -> ability.name == name }
            ?: name.toIntOrNull()?.toFirebaseRollAbility()
            ?: RollAbility.REQUIRED
    }

    return when (this) {
        is Int -> toFirebaseRollAbility()
        is Long -> toInt().toFirebaseRollAbility()
        is Short -> toInt().toFirebaseRollAbility()
        is Byte -> toInt().toFirebaseRollAbility()
        is Double -> toInt().toFirebaseRollAbility()
        is Float -> toInt().toFirebaseRollAbility()
        else -> RollAbility.REQUIRED
    }
}

private fun Int.toFirebaseRollAbility(): RollAbility =
    RollAbility.entries.getOrElse(this) { RollAbility.REQUIRED }
