package com.alextos.thousand.data.mappers

import com.alextos.thousand.domain.models.Effect

internal fun Effect.toStorageValue(): String = when (this) {
    Effect.OVERTAKE -> "OVERTAKE"
    Effect.SKI_FALL -> "SKI_FALL"
    Effect.PIT_FALL -> "PIT_FALL"
    Effect.BARREL_LIMIT -> "BARREL_LIMIT"
}

internal fun String.toDomainEffect(): Effect = when (this) {
    "OVERTAKE" -> Effect.OVERTAKE
    "SKI_FALL" -> Effect.SKI_FALL
    "PIT_FALL" -> Effect.PIT_FALL
    "BARREL_LIMIT" -> Effect.BARREL_LIMIT
    else -> error("Unsupported effect type: $this")
}
