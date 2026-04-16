package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.DieEntity
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue

fun Die.toEntity(
    playerId: Long,
    rollId: Long,
    order: Int
): DieEntity = DieEntity(
    id = id,
    playerId = playerId,
    rollId = rollId,
    order = order,
    value = value.value,
)

fun DieEntity.toDomain(): Die = Die(
    id = id,
    order = order,
    value = value.toDomainDieValue(),
)

private fun Int.toDomainDieValue(): DieValue = when (this) {
    1 -> DieValue.ONE
    2 -> DieValue.TWO
    3 -> DieValue.THREE
    4 -> DieValue.FOUR
    5 -> DieValue.FIVE
    6 -> DieValue.SIX
    else -> error("Unsupported die value: $this")
}
