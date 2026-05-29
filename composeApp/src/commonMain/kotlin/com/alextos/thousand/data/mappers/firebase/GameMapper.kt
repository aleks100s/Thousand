package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.RemoteGame

object FirebaseGameMapper {
    fun dictionary(from: RemoteGame): Map<String, Any> = from.toFirebaseMap()

    fun game(from: Any?, key: String?): RemoteGame? = from.asFirebaseMap()?.toFirebaseGame(key)
}

internal fun RemoteGame.toFirebaseMap(): Map<String, Any> =
    buildMap {
        put("id", id)
        put("settings", settings.toFirebaseMap())
        put("players", players.map { player -> player.toFirebaseMap() })
        put("host", host)
    }

internal fun Map<*, *>.toFirebaseGame(key: String?): RemoteGame =
    RemoteGame(
        id = long("id") ?: 0L,
        settings = get("settings").asFirebaseMap().toFirebaseGameSettings(),
        players = get("players").asFirebaseMapList().map { player -> player.toFirebasePlayer() },
        host = string("host").orEmpty(),
        key = key.orEmpty()
    )
