package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.GameButton

internal fun Any?.toFirebaseGameButton(): GameButton? {
    val name = this as? String
    if (name != null) {
        return GameButton.entries.firstOrNull { button -> button.name == name }
            ?: name.toIntOrNull()?.toFirebaseGameButton()
    }

    return when (this) {
        is Int -> toFirebaseGameButton()
        is Long -> toInt().toFirebaseGameButton()
        is Short -> toInt().toFirebaseGameButton()
        is Byte -> toInt().toFirebaseGameButton()
        is Double -> toInt().toFirebaseGameButton()
        is Float -> toInt().toFirebaseGameButton()
        else -> null
    }
}

private fun Int.toFirebaseGameButton(): GameButton? =
    GameButton.entries.getOrNull(this)
