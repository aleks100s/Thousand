package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.GameEntity
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    startedAt = startedAt,
    finishedAt = finishedAt,
)

fun GameEntity.toDomain(
    players: List<Player> = emptyList(),
    turns: List<Turn> = emptyList(),
): Game = Game(
    id = id,
    startedAt = startedAt,
    finishedAt = finishedAt,
    players = players,
    turns = turns,
)
