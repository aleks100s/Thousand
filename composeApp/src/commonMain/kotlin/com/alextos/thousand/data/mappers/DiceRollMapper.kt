package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.DiceRollEntity
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die

fun DiceRoll.toEntity(
    playerId: Int,
    turnId: Int,
): DiceRollEntity = DiceRollEntity(
    id = id,
    playerId = playerId,
    turnId = turnId,
    order = order,
    total = result,
)

fun DiceRollEntity.toDomain(
    dice: List<Die> = emptyList(),
): DiceRoll = DiceRoll(
    id = id,
    order = order,
    dice = dice,
    result = total,
)
