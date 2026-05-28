package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.Game

object FirebaseGameMapper {
    fun dictionary(from: Game): Map<String, Any> = from.toFirebaseMap()

    fun game(from: Any?): Game? = from.asFirebaseMap()?.toFirebaseGame()
}

internal fun Game.toFirebaseMap(): Map<String, Any> =
    buildMap {
        put("id", id)
        put("settings", settings.toFirebaseMap())
        put("players", players.map { player -> player.toFirebaseMap() })
        put("host", host)
        put("key", key)
    }

internal fun Map<*, *>.toFirebaseGame(): Game =
    Game(
        id = long("id") ?: 0L,
        settings = get("settings").asFirebaseMap().toFirebaseGameSettings(),
        players = get("players").asFirebaseMapList().map { player -> player.toFirebasePlayer() },
        host = string("host") ?: "",
        key = string("key") ?: ""
    )
