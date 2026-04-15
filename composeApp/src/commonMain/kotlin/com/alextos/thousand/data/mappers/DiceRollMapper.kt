package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.DiceRollEntity
import com.alextos.thousand.data.models.combined.DiceRollWithDice
import com.alextos.thousand.domain.models.DiceRoll

fun DiceRoll.toEntity(
    userId: Long,
    turnId: Long,
): DiceRollEntity = DiceRollEntity(
    id = id,
    userId = userId,
    turnId = turnId,
    order = order,
    total = result,
)

fun DiceRollWithDice.toDomain(): DiceRoll = DiceRoll(
    id = diceRoll.id,
    order = diceRoll.order,
    dice = dice.map { it.toDomain() },
    result = diceRoll.total,
)
