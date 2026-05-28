package com.alextos.thousand.data.repository.mappers

import com.alextos.thousand.data.mappers.firebase.FirebaseGameMapper
import com.alextos.thousand.domain.models.Game
import com.google.firebase.database.DataSnapshot

internal fun DataSnapshot.toGame(): Game? {
    if (!exists()) return null

    return FirebaseGameMapper.game(from = value)
}

internal fun Game.toDatabaseMap(): Map<String, Any> =
    FirebaseGameMapper.dictionary(from = this)
