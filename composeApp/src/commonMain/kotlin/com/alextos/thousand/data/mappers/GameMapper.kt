package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.GameEntity
import com.alextos.thousand.data.models.combined.GameWithRelations
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Turn

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    startedAt = startedAt,
    finishedAt = finishedAt,
)

fun GameWithRelations.toDomain(turns: List<Turn> = emptyList()): Game = Game(
    id = game.id,
    startedAt = game.startedAt,
    finishedAt = game.finishedAt,
    players = players.map { it.toDomain() },
    turns = turns
)
