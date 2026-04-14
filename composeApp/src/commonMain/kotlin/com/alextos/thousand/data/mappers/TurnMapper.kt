package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.TurnEntity
import com.alextos.thousand.data.models.combined.TurnWithRelations
import com.alextos.thousand.domain.models.Turn

fun Turn.toEntity(
    userId: Int,
    gameId: Int,
): TurnEntity = TurnEntity(
    id = id,
    userId = userId,
    gameId = gameId,
    order = order,
    total = total,
)

fun TurnWithRelations.toDomain(): Turn = Turn(
    id = turn.id,
    order = turn.order,
    user = user,
    rolls = rolls.map { it.toDomain() },
    total = turn.total,
    effects = effects.map { it.turnEffect.effectType.toDomainEffect() },
)
