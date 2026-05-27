package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.Lobby

object FirebaseLobbyMapper {
    fun dictionary(from: Lobby): Map<String, Any> =
        from.toFirebaseMap()

    fun lobby(from: Any?, fallbackId: String = ""): Lobby? =
        from.asFirebaseMap()?.toFirebaseLobby(fallbackId)
}

internal fun Lobby.toFirebaseMap(): Map<String, Any> =
    mapOf(
        "id" to id,
        "settings" to settings.toFirebaseMap(),
        "players" to players.map { player -> player.toFirebaseLobbyPlayerMap() },
        "host" to host,
        "game" to game,
    )

internal fun Map<*, *>.toFirebaseLobby(fallbackId: String = ""): Lobby =
    Lobby(
        id = string("id") ?: fallbackId,
        settings = get("settings").asFirebaseMap().toFirebaseGameSettings(),
        players = get("players").asFirebaseMapList().map { player -> player.toFirebaseLobbyPlayer() },
        host = string("host").orEmpty(),
        game = string("game").orEmpty(),
    )
