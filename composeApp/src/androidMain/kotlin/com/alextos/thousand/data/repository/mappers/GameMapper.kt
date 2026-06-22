package com.alextos.thousand.data.repository.mappers

import com.alextos.thousand.data.mappers.firebase.FirebaseGameMapper
import com.alextos.thousand.domain.models.RemoteGame
import com.google.firebase.database.DataSnapshot

internal fun DataSnapshot.toGame(): RemoteGame? {
    if (!exists()) return null

    return FirebaseGameMapper.game(from = value, key)
}

internal fun RemoteGame.toDatabaseMap(): Map<String, Any> =
    FirebaseGameMapper.dictionary(from = this)

internal fun RemoteGame.toDatabaseUpdateMap(): Map<String, Any?> =
    toDatabaseMap()
        .filterKeys { key -> key != "onlinePlayerIds" }
        .mapValues { entry -> entry.value as Any? }
        .toMutableMap()
        .apply {
            if (reaction == null) {
                put("reaction", null)
            }
        }
