package com.alextos.thousand.data.mappers.room

import com.alextos.thousand.domain.models.PlayerAvatar
import com.alextos.thousand.domain.models.PlayerColor

import com.alextos.thousand.data.models.UserEntity
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    kind = kind.ordinal,
    color = color.name,
    avatar = avatar.name,
)

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    kind = UserKind.entries.getOrElse(kind) { UserKind.LocalUser },
    color = PlayerColor.entries.firstOrNull { it.name == color }
        ?: PlayerColor.defaultFor(id, UserKind.entries.getOrElse(kind) { UserKind.LocalUser }),
    avatar = PlayerAvatar.entries.firstOrNull { it.name == avatar }
        ?: PlayerAvatar.Neutral,
)
