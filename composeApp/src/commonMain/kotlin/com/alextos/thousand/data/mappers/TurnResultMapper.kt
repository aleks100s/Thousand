package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.TurnResultEntity
import com.alextos.thousand.data.models.combined.TurnResultWithPlayer
import com.alextos.thousand.domain.models.TurnResult

fun TurnResult.toEntity(turnId: Long): TurnResultEntity = TurnResultEntity(
    id = id,
    turnId = turnId,
    playerId = player.id,
    scoreChange = scoreChange,
    newScore = newScore,
)

fun TurnResultWithPlayer.toDomain(): TurnResult = TurnResult(
    id = turnResult.id,
    player = player.toDomain(),
    scoreChange = turnResult.scoreChange,
    newScore = turnResult.newScore,
)
