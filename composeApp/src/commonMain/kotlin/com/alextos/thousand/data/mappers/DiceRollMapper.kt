package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.DiceRollEntity
import com.alextos.thousand.data.models.combined.DiceRollWithDice
import com.alextos.thousand.domain.models.DiceRoll

fun DiceRoll.toEntity(
    playerId: Long,
    turnId: Long,
    order: Int
): DiceRollEntity = DiceRollEntity(
    id = id,
    playerId = playerId,
    turnId = turnId,
    order = order,
    total = result,
)

fun DiceRollWithDice.toDomain(): DiceRoll = DiceRoll(
    id = diceRoll.id,
    dice = dice
        .sortedBy { it.order }
        .map { it.toDomain() },
    result = diceRoll.total,
)
