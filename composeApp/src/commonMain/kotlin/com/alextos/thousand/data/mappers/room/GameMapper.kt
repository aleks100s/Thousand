package com.alextos.thousand.data.mappers.room

import com.alextos.thousand.data.models.GameEntity
import com.alextos.thousand.data.models.GameSettingsEntity
import com.alextos.thousand.data.models.combined.GameWithRelations
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.GameSettings
import kotlin.time.Instant

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    startedAt = startedAt.toEpochMilliseconds(),
    finishedAt = finishedAt?.toEpochMilliseconds(),
)

fun GameSettings.toEntity(gameId: Long): GameSettingsEntity = GameSettingsEntity(
    gameId = gameId,
    isNotificationEnabled = isNotificationEnabled,
    isVirtualDiceEnabled = isVirtualDiceEnabled,
    isShakeEnabled = isShakeEnabled,
    hasStartLimit = hasStartLimit,
    isBarrel1Active = isBarrel1Active,
    isBarrel2Active = isBarrel2Active,
    isBarrel3Active = isBarrel3Active,
    isTripleBoltFineActive = isTripleBoltFineActive,
    isOvertakeFineActive = isOvertakeFineActive,
)

fun GameWithRelations.toDomain(): Game = Game(
    id = game.id,
    startedAt = Instant.fromEpochMilliseconds(game.startedAt),
    finishedAt = game.finishedAt?.let(Instant::fromEpochMilliseconds),
    settings = settings.toDomain(),
    players = players.map { it.toDomain() }
)

private fun GameSettingsEntity.toDomain(): GameSettings = GameSettings(
    isNotificationEnabled = isNotificationEnabled,
    isVirtualDiceEnabled = isVirtualDiceEnabled,
    isShakeEnabled = isShakeEnabled,
    hasStartLimit = hasStartLimit,
    isBarrel1Active = isBarrel1Active,
    isBarrel2Active = isBarrel2Active,
    isBarrel3Active = isBarrel3Active,
    isTripleBoltFineActive = isTripleBoltFineActive,
    isOvertakeFineActive = isOvertakeFineActive,
)
