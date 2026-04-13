package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.TurnEntity
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.Turn

fun Turn.toEntity(
    playerId: Int,
    gameId: Int,
): TurnEntity = TurnEntity(
    id = id,
    playerId = playerId,
    gameId = gameId,
    order = order,
    total = total,
)

fun TurnEntity.toDomain(
    rolls: List<DiceRoll> = emptyList(),
    effects: List<Effect> = emptyList(),
): Turn = Turn(
    id = id,
    order = order,
    rolls = rolls,
    total = total,
    effects = effects,
)
