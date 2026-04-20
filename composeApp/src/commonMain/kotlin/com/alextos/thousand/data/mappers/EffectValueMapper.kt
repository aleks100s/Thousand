package com.alextos.thousand.data.mappers

import com.alextos.thousand.domain.models.Effect

internal fun Effect.toStorageValue(): String = name

internal fun String.toDomainEffect(): Effect = Effect.valueOf(this)
