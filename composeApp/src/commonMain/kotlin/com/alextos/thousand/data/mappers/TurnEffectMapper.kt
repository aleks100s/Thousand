package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.TurnEffectEntity
import com.alextos.thousand.data.models.combined.TurnEffectWithPlayer
import com.alextos.thousand.domain.models.TurnEffect

fun TurnEffect.toEntity(turnId: Long): TurnEffectEntity = TurnEffectEntity(
    id = id,
    turnId = turnId,
    effectType = effect.toStorageValue(),
    affectedPlayerId = affectedPlayer.id,
)

fun TurnEffectWithPlayer.toDomain(
    penaltyValue: Int = 0,
): TurnEffect = TurnEffect(
    id = turnEffect.id,
    affectedPlayer = requireNotNull(affectedPlayer) {
        "TurnEffect ${turnEffect.id} is missing affected player relation"
    }.toDomain(),
    effect = turnEffect.effectType.toDomainEffect(),
    penaltyValue = penaltyValue,
)
