package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.PlayerEntity
import com.alextos.thousand.data.models.combined.PlayerWithUser
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.User

fun Player.toEntity(gameId: Long): PlayerEntity = PlayerEntity(
    id = id,
    userId = user.id,
    gameId = gameId,
    currentScore = currentScore,
    isWinner = isWinner,
)

fun PlayerEntity.toDomain(user: User): Player = Player(
    id = id,
    user = user,
    currentScore = currentScore,
    isWinner = isWinner,
)

fun PlayerWithUser.toDomain(): Player = player.toDomain(
    user = user.toDomain(),
)
