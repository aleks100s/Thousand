package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.UserReaction

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
        put("rollAbility", rollAbility.name)
        put("buttons", buttons.map { button -> button.name })
        put("messagesToShow", messagesToShow)
        put("currentPlayerIndex", currentPlayerIndex)
        put("onlinePlayerIds", onlinePlayerIds.associateWith { true })

        reaction?.let { reaction ->
            put("reaction", reaction.toFirebaseMap())
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
        currentPlayerIndex = int("currentPlayerIndex") ?: 0,
        currentTurn = get("currentTurn").asFirebaseMapList().map { roll -> roll.toFirebaseDiceRoll() },
        currentRoll = get("currentRoll").asFirebaseMap()?.toFirebaseDiceRoll(),
        rollAbility = get("rollAbility").toFirebaseRollAbility(),
        buttons = when (val value = get("buttons")) {
            is List<*> -> value.mapNotNull { item -> item.toFirebaseGameButton() }
            is Map<*, *> -> value.entries
                .sortedWith(
                    compareBy<Map.Entry<*, *>> { entry -> entry.key.toString().toIntOrNull() ?: Int.MAX_VALUE }
                        .thenBy { entry -> entry.key.toString() },
                )
                .mapNotNull { entry -> entry.value.toFirebaseGameButton() }
            else -> emptyList()
        },
        messagesToShow = get("messagesToShow").asFirebaseStringList(),
        reaction = get("reaction").toFirebaseUserReaction(),
        onlinePlayerIds = get("onlinePlayerIds").asFirebaseOnlinePlayerIds(),
    )
}

private fun Any?.asFirebaseOnlinePlayerIds(): Set<String> =
    asFirebaseMap()
        ?.entries
        ?.mapNotNull { entry ->
            val userId = entry.key as? String ?: return@mapNotNull null

            if (entry.value == true) {
                userId
            } else {
                null
            }
        }
        ?.toSet()
        ?: emptySet()

private fun UserReaction.toFirebaseMap(): Map<String, Any> =
    mapOf(
        "id" to id,
        "authorId" to authorId,
        "author" to author,
        "reaction" to reaction,
    )

private fun Any?.toFirebaseUserReaction(): UserReaction? =
    asFirebaseMap()?.let { reaction ->
        val reactionValue = reaction.string("reaction") ?: return@let null
        val id = reaction.string("id")

        if (id.isNullOrBlank()) {
            UserReaction(
                authorId = reaction.string("authorId").orEmpty(),
                author = reaction.string("author").orEmpty(),
                reaction = reactionValue,
            )
        } else {
            UserReaction(
                id = id,
                authorId = reaction.string("authorId").orEmpty(),
                author = reaction.string("author").orEmpty(),
                reaction = reactionValue,
            )
        }
    }
