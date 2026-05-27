package com.alextos.thousand.data.repository.mappers

import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.Lobby
import com.google.firebase.database.DataSnapshot

internal fun DataSnapshot.toLobby(): Lobby? {
    if (!exists()) return null

    return Lobby(
        id = child("id").getValue(String::class.java) ?: "",
        settings = child("settings").toGameSettings(),
        players = child("players").children.map { playerSnapshot ->
            playerSnapshot.toUser()
        },
        host = child("host").getValue(String::class.java).orEmpty(),
        game = child("game").getValue(String::class.java).orEmpty(),
    )
}

internal fun Lobby.toDatabaseMap(): Map<String, Any?> =
    mapOf(
        "id" to id,
        "settings" to settings.toDatabaseMap(),
        "players" to players.map { player -> player.toLobbyPlayerMap() },
        "host" to host,
        "game" to game,
    )

internal fun GameSettings.toDatabaseMap(): Map<String, Any> =
    mapOf(
        "isNotificationEnabled" to isNotificationEnabled,
        "isVirtualDiceEnabled" to isVirtualDiceEnabled,
        "isShakeEnabled" to isShakeEnabled,
        "hasStartLimit" to hasStartLimit,
        "isBarrel1Active" to isBarrel1Active,
        "isBarrel2Active" to isBarrel2Active,
        "isBarrel3Active" to isBarrel3Active,
        "isTripleBoltFineActive" to isTripleBoltFineActive,
        "isOvertakeFineActive" to isOvertakeFineActive,
    )

internal fun DataSnapshot.toGameSettings(): GameSettings =
    GameSettings(
        isNotificationEnabled = child("isNotificationEnabled").getValue(Boolean::class.java) ?: true,
        isVirtualDiceEnabled = child("isVirtualDiceEnabled").getValue(Boolean::class.java) ?: true,
        isShakeEnabled = child("isShakeEnabled").getValue(Boolean::class.java) ?: true,
        hasStartLimit = child("hasStartLimit").getValue(Boolean::class.java) ?: true,
        isBarrel1Active = child("isBarrel1Active").getValue(Boolean::class.java) ?: true,
        isBarrel2Active = child("isBarrel2Active").getValue(Boolean::class.java) ?: true,
        isBarrel3Active = child("isBarrel3Active").getValue(Boolean::class.java) ?: false,
        isTripleBoltFineActive = child("isTripleBoltFineActive").getValue(Boolean::class.java) ?: true,
        isOvertakeFineActive = child("isOvertakeFineActive").getValue(Boolean::class.java) ?: true,
    )
