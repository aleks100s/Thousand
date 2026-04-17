package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.TurnEntity
import com.alextos.thousand.data.models.combined.TurnWithRelations
import com.alextos.thousand.domain.models.Turn

fun Turn.toEntity(
    gameId: Long,
): TurnEntity = TurnEntity(
    id = id,
    playerId = player.id,
    gameId = gameId,
    order = order,
    total = total,
)

fun TurnWithRelations.toDomain(): Turn = Turn(
    id = turn.id,
    order = turn.order,
    player = player.toDomain(),
    rolls = rolls
        .sortedBy { it.diceRoll.order }
        .map { it.toDomain() },
    total = turn.total,
    effects = effects
        .sortedBy { it.turnEffect.order }
        .map { it.toDomain() },
    results = results
        .sortedBy { it.turnResult.id }
        .map { it.toDomain() },
)
