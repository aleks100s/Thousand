package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.Game

object FirebaseGameMapper {
    fun dictionary(from: Game): Map<String, Any> =
        from.toFirebaseMap()

    fun game(from: Any?, fallbackId: String = ""): Game? =
        from.asFirebaseMap()?.toFirebaseGame(fallbackId)
}

internal fun Game.toFirebaseMap(): Map<String, Any> =
    buildMap {
        put("id", id)
        put("startedAt", startedAt.toEpochMilliseconds())
        put("settings", settings.toFirebaseMap())
        put("players", players.map { player -> player.toFirebaseMap() })

        finishedAt?.let { value ->
            put("finishedAt", value.toEpochMilliseconds())
        }
    }

internal fun Map<*, *>.toFirebaseGame(fallbackId: String = ""): Game =
    Game(
        id = long("id") ?: fallbackId.toLongOrNull() ?: 0L,
        startedAt = FirebaseValueMapper.instant(get("startedAt")),
        finishedAt = FirebaseValueMapper.optionalInstant(get("finishedAt")),
        settings = get("settings").asFirebaseMap().toFirebaseGameSettings(),
        players = get("players").asFirebaseMapList().map { player -> player.toFirebasePlayer() },
    )
