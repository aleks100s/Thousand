package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.Game

object FirebaseGameMapper {
    fun dictionary(from: Game): Map<String, Any> = from.toFirebaseMap()

    fun game(from: Any?, key: String?): Game? = from.asFirebaseMap()?.toFirebaseGame(key)
}

internal fun Game.toFirebaseMap(): Map<String, Any> =
    buildMap {
        put("id", id)
        put("settings", settings.toFirebaseMap())
        put("players", players.map { player -> player.toFirebaseMap() })
        put("host", host)
    }

internal fun Map<*, *>.toFirebaseGame(key: String?): Game =
    Game(
        id = long("id") ?: 0L,
        settings = get("settings").asFirebaseMap().toFirebaseGameSettings(),
        players = get("players").asFirebaseMapList().map { player -> player.toFirebasePlayer() },
        host = string("host") ?: "",
        key = key ?: ""
    )
