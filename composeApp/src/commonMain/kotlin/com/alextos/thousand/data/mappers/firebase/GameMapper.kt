package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.User

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
        put("currentTurn", currentTurn.map { roll -> roll.toFirebaseMap() })

        currentPlayer?.let { player ->
            put("currentPlayer", player.toFirebaseMap())
        }

        currentRoll?.let { roll ->
            put("currentRoll", roll.toFirebaseMap())
        }
    }

internal fun Map<*, *>.toFirebaseGame(key: String?): RemoteGame {
    val players = get("players").asFirebaseMapList().map { player -> player.toFirebasePlayer() }

    return RemoteGame(
        id = long("id") ?: 0L,
        settings = get("settings").asFirebaseMap().toFirebaseGameSettings(),
        players = players,
        host = string("host").orEmpty(),
        key = key.orEmpty(),
        currentPlayer = get("currentPlayer").asFirebaseMap()?.toFirebasePlayer()
            ?: players.firstOrNull()
            ?: com.alextos.thousand.domain.models.Player(user = User(name = "Без имени")),
        currentTurn = get("currentTurn").asFirebaseMapList().map { roll -> roll.toFirebaseDiceRoll() },
        currentRoll = get("currentRoll").asFirebaseMap()?.toFirebaseDiceRoll(),
    )
}
