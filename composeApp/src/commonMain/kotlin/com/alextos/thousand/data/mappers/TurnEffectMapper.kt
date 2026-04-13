package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.TurnEffectEntity
import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.TurnEffect

fun TurnEffect.toEntity(turnId: Int): TurnEffectEntity = TurnEffectEntity(
    id = id,
    turnId = turnId,
    effectType = effect.toStorageValue(),
    affectedPlayerId = affectedPlayer.id,
)

fun TurnEffectEntity.toDomain(
    affectedPlayer: Player,
    penaltyValue: Int = 0,
): TurnEffect = TurnEffect(
    id = id,
    affectedPlayer = affectedPlayer,
    effect = effectType.toDomainEffect(),
    penaltyValue = penaltyValue,
)

private fun Effect.toStorageValue(): String = when (this) {
    Effect.OVERTAKE -> "OVERTAKE"
    Effect.SKI_FALL -> "SKI_FALL"
    Effect.PIT_FALL -> "PIT_FALL"
    Effect.BARREL_LIMIT -> "BARREL_LIMIT"
}

private fun String.toDomainEffect(): Effect = when (this) {
    "OVERTAKE" -> Effect.OVERTAKE
    "SKI_FALL" -> Effect.SKI_FALL
    "PIT_FALL" -> Effect.PIT_FALL
    "BARREL_LIMIT" -> Effect.BARREL_LIMIT
    else -> error("Unsupported effect type: $this")
}
