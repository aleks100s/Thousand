package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.UserEntity
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    kind = kind.ordinal,
)

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    kind = UserKind.entries.getOrElse(kind) { UserKind.LocalUser },
)
