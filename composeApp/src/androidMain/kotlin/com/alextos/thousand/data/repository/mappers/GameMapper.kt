package com.alextos.thousand.data.repository.mappers

import com.alextos.thousand.data.mappers.firebase.FirebaseGameMapper
import com.alextos.thousand.data.mappers.firebase.FirebaseValueMapper
import com.alextos.thousand.domain.models.Game
import com.google.firebase.database.DataSnapshot
import kotlin.time.Instant

internal fun DataSnapshot.toGame(): Game? {
    if (!exists()) return null

    return FirebaseGameMapper.game(
        from = getValue(),
        fallbackId = key.orEmpty(),
    )
}

internal fun Game.toDatabaseMap(): Map<String, Any> =
    FirebaseGameMapper.dictionary(from = this)

internal fun DataSnapshot.instantValue(): Instant? =
    FirebaseValueMapper.optionalInstant(from = getValue())
