package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.GameEntity
import com.alextos.thousand.data.models.combined.GameWithRelations
import com.alextos.thousand.domain.models.Game
import kotlin.time.Instant

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    startedAt = startedAt.toEpochMilliseconds(),
    finishedAt = finishedAt?.toEpochMilliseconds(),
    isShakeEnabled = isShakeEnabled,
    isVirtualDiceEnabled = isVirtualDiceEnabled,
    isNotificationEnabled = isNotificationEnabled,
)

fun GameWithRelations.toDomain(): Game = Game(
    id = game.id,
    startedAt = Instant.fromEpochMilliseconds(game.startedAt),
    finishedAt = game.finishedAt?.let(Instant::fromEpochMilliseconds),
    isShakeEnabled = game.isShakeEnabled,
    isVirtualDiceEnabled = game.isVirtualDiceEnabled,
    isNotificationEnabled = game.isNotificationEnabled,
    players = players.map { it.toDomain() }
)
