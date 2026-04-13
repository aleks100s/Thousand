package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.UserEntity
import com.alextos.thousand.domain.models.User

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
)

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
)
