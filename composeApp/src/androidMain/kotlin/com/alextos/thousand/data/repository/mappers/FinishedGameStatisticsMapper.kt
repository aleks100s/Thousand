package com.alextos.thousand.data.repository.mappers

import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.RemoteUserInfo
import com.google.firebase.database.ServerValue

private const val USERS_NODE = "users"
private const val TOTAL_GAMES_COUNT_NODE = "gameCount"
private const val WIN_COUNT_NODE = "winCount"
private const val RATING_NODE = "rating"

internal fun RemoteGame.toFinishedGameStatisticsUpdates(
    userInfo: Map<String, RemoteUserInfo>
): Map<String, Any> {
    val updates = mutableMapOf<String, Any>()

    players.forEach { player ->
        val userId = player.user.id
        if (userId.isBlank()) {
            return@forEach
        }

        updates["$USERS_NODE/$userId/$TOTAL_GAMES_COUNT_NODE"] = ServerValue.increment(1)
        if (player.isWinner) {
            updates["$USERS_NODE/$userId/$WIN_COUNT_NODE"] = ServerValue.increment(1)
        }

        userInfo[player.user.id]?.let { remoteUserInfo ->
            updates["$USERS_NODE/$userId/$RATING_NODE"] = remoteUserInfo.rating
        }
    }

    return updates
}
