package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.Player

object FirebasePlayerMapper {
    fun dictionary(from: Player): Map<String, Any> =
        from.toFirebaseMap()

    fun player(from: Any?): Player =
        from.asFirebaseMap().toFirebasePlayer()
}

internal fun Player.toFirebaseMap(): Map<String, Any> =
    mapOf(
        "id" to id,
        "user" to user.toFirebaseMap(),
        "currentScore" to currentScore,
        "isWinner" to isWinner,
        "boltCount" to boltCount,
        "hasPassedStartLimit" to hasPassedStartLimit,
    )

internal fun Map<*, *>?.toFirebasePlayer(): Player {
    val userMap = this?.get("user").asFirebaseMap()
    val user = userMap?.toFirebaseUser() ?: toFirebaseLobbyPlayer()

    return Player(
        id = long("id") ?: 0L,
        user = user,
        currentScore = int("currentScore") ?: 0,
        isWinner = boolean("isWinner", false),
        boltCount = int("boltCount") ?: 0,
        hasPassedStartLimit = boolean("hasPassedStartLimit", false),
    )
}
