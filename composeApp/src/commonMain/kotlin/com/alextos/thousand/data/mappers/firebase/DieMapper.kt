package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue

internal fun Die.toFirebaseMap(): Map<String, Any> =
    mapOf(
        "id" to id,
        "value" to value.name,
    )

internal fun Map<*, *>?.toFirebaseDie(): Die =
    Die(
        id = long("id") ?: 0L,
        value = this?.get("value").toFirebaseDieValue(),
    )

private fun Any?.toFirebaseDieValue(): DieValue {
    val name = this as? String
    if (name != null) {
        name.toIntOrNull()?.let { value ->
            return value.toFirebaseDieValue()
        }

        return DieValue.entries.firstOrNull { value -> value.name == name }
            ?: DieValue.ONE
    }

    return when (this) {
        is Int -> toFirebaseDieValue()
        is Long -> toInt().toFirebaseDieValue()
        is Short -> toInt().toFirebaseDieValue()
        is Byte -> toInt().toFirebaseDieValue()
        is Double -> toInt().toFirebaseDieValue()
        is Float -> toInt().toFirebaseDieValue()
        else -> DieValue.ONE
    }
}

private fun Int.toFirebaseDieValue(): DieValue =
    when (this) {
        in 1..6 -> DieValue.entries[this - 1]
        in DieValue.entries.indices -> DieValue.entries[this]
        else -> DieValue.ONE
    }
