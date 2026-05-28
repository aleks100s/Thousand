package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.Lobby

object FirebaseLobbyMapper {
    fun dictionary(from: Lobby): Map<String, Any> = from.toFirebaseMap()

    fun lobby(from: Any?, key: String?): Lobby? = from.asFirebaseMap()?.toFirebaseLobby(key)
}

internal fun Lobby.toFirebaseMap(): Map<String, Any> =
    mapOf(
        "id" to id,
        "settings" to settings.toFirebaseMap(),
        "players" to players.map { player -> player.toFirebaseLobbyPlayerMap() },
        "host" to host,
        "game" to game
    )

internal fun Map<*, *>.toFirebaseLobby(key: String?): Lobby =
    Lobby(
        id = string("id").orEmpty(),
        settings = get("settings").asFirebaseMap().toFirebaseGameSettings(),
        players = get("players").asFirebaseMapList().map { player -> player.toFirebaseLobbyPlayer() },
        host = string("host").orEmpty(),
        game = string("game").orEmpty(),
        key = key.orEmpty()
    )
