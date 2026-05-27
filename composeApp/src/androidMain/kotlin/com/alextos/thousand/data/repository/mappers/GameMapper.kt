package com.alextos.thousand.data.repository.mappers

import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.google.firebase.database.DataSnapshot
import kotlin.time.Clock
import kotlin.time.Instant

internal fun DataSnapshot.toGame(): Game? {
    if (!exists()) return null

    return Game(
        id = child("id").longValue() ?: key?.toLongOrNull() ?: 0L,
        startedAt = Clock.System.now(),
        finishedAt = null,
        settings = child("settings").toGameSettings(),
        players = child("players").children.map { playerSnapshot ->
            Player(
                id = playerSnapshot.child("id").longValue() ?: 0L,
                user = playerSnapshot.child("user").toUser(),
                currentScore = playerSnapshot.child("currentScore").intValue() ?: 0,
                isWinner = playerSnapshot.child("isWinner").getValue(Boolean::class.java) ?: false,
                boltCount = playerSnapshot.child("boltCount").intValue() ?: 0,
                hasPassedStartLimit = playerSnapshot.child("hasPassedStartLimit")
                    .getValue(Boolean::class.java) ?: false,
            )
        },
    )
}

internal fun Game.toDatabaseMap(): Map<String, Any?> =
    mapOf(
        "id" to id,
        "startedAt" to startedAt.toEpochMilliseconds(),
        "finishedAt" to finishedAt?.toEpochMilliseconds(),
        "settings" to settings.toDatabaseMap(),
        "players" to players.map { player -> player.toDatabaseMap() },
    )

private fun Player.toDatabaseMap(): Map<String, Any?> =
    mapOf(
        "id" to id,
        "user" to user.toDatabaseMap(),
        "currentScore" to currentScore,
        "isWinner" to isWinner,
        "boltCount" to boltCount,
        "hasPassedStartLimit" to hasPassedStartLimit,
    )

private fun DataSnapshot.instantValue(): Instant? {
    longValue()?.let { epochMilliseconds ->
        return Instant.fromEpochMilliseconds(epochMilliseconds)
    }

    val epochSeconds = child("epochSeconds").longValue()
    if (epochSeconds != null) {
        return Instant.fromEpochSeconds(
            epochSeconds = epochSeconds,
            nanosecondAdjustment = child("nanosecondsOfSecond").intValue() ?: 0,
        )
    }

    return getValue(String::class.java)?.let { value ->
        runCatching { Instant.parse(value) }.getOrNull()
    }
}
