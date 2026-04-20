package com.alextos.thousand.data.mappers

import com.alextos.thousand.domain.models.Effect

internal fun Effect.toStorageValue(): String = when (this) {
    Effect.OVERTAKE -> "OVERTAKE"
    Effect.TRIPLE_BOLT -> "SKI_FALL"
    Effect.PIT_FALL -> "PIT_FALL"
    Effect.BARREL_LIMIT -> "BARREL_LIMIT"
    Effect.WIN -> "WIN"
}

internal fun String.toDomainEffect(): Effect = when (this) {
    "OVERTAKE" -> Effect.OVERTAKE
    "SKI_FALL" -> Effect.TRIPLE_BOLT
    "PIT_FALL" -> Effect.PIT_FALL
    "BARREL_LIMIT" -> Effect.BARREL_LIMIT
    "WIN" -> Effect.WIN
    else -> error("Unsupported effect type: $this")
}
