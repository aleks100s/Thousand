package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.RemoteAchievementTurn
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.UserReaction
import kotlin.time.Instant

object FirebaseGameMapper {
    fun dictionary(from: RemoteGame): Map<String, Any> = from.toFirebaseMap()

    fun reactionDictionary(from: UserReaction): Map<String, Any> = from.toFirebaseMap()

    fun game(from: Any?, key: String?): RemoteGame? = from.asFirebaseMap()?.toFirebaseGame(key)
}

internal fun RemoteGame.toFirebaseMap(): Map<String, Any> =
    buildMap {
        put("lastGameplayAt", mapOf(".sv" to "timestamp"))
        put("id", id)
        put("settings", settings.toFirebaseMap())
        put("players", players.map { player -> player.toFirebaseMap() })
        put("host", host)
        put("memberIds", players.mapIndexed { index, player -> player.user.id to index.toString() }.toMap())
        put("currentTurn", currentTurn.map { roll -> roll.toFirebaseMap() })
        put("rollAbility", rollAbility.name)
        put("buttons", buttons.map { button -> button.name })
        put("messagesToShow", messagesToShow)
        put("currentPlayerIndex", currentPlayerIndex)
        put("onlinePlayerIds", onlinePlayerIds.associateWith { true })

        achievementTurn?.let { turn ->
            put("achievementTurn", turn.toFirebaseMap())
        }

        reaction?.let { reaction ->
            put("reaction", reaction.toFirebaseMap())
        }

        currentRoll?.let { roll ->
            put("currentRoll", roll.toFirebaseMap())
        }
    }

internal fun Map<*, *>.toFirebaseGame(key: String?): RemoteGame {
    val players = get("players").asFirebaseMapList().map { player -> player.toFirebasePlayer() }
    val currentPlayerIndex = int("currentPlayerIndex") ?: 0
    // Older clients can leave a previous finish timestamp behind after a rematch.
    val finishedAt = long("finishedAt")?.takeIf {
        currentPlayerIndex == -1 && it == long("lastGameplayAt")
    }

    return RemoteGame(
        id = long("id") ?: 0L,
        settings = get("settings").asFirebaseMap().toFirebaseGameSettings(),
        players = players,
        host = string("host").orEmpty(),
        key = key.orEmpty(),
        currentPlayerIndex = currentPlayerIndex,
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
        finishedAt = finishedAt?.let(Instant::fromEpochMilliseconds),
        achievementTurn = get("achievementTurn").asFirebaseMap()
            ?.takeIf { currentPlayerIndex >= 0 || (finishedAt != null && it.long("finishedAt") == finishedAt) }
            ?.toAchievementTurn()
            ?.takeIf { it.nextPlayerIndex == currentPlayerIndex },
    )
}

private fun RemoteAchievementTurn.toFirebaseMap(): Map<String, Any> = buildMap {
    put("roundId", roundId)
    put("number", number)
    put("nextPlayerIndex", nextPlayerIndex)
    put("total", total)
    put("hasCompleteNumbering", hasCompleteNumbering)
    if (nextPlayerIndex == -1) put("finishedAt", mapOf(".sv" to "timestamp"))
    playerId?.let { put("playerId", it) }
    if (pitPlayerIds.isNotEmpty()) put("pitPlayerIds", pitPlayerIds.toList())
}

private fun Map<*, *>.toAchievementTurn(): RemoteAchievementTurn? {
    return RemoteAchievementTurn(
        roundId = string("roundId")?.takeIf { it.isNotBlank() } ?: return null,
        number = int("number")?.takeIf { it >= 0 } ?: return null,
        nextPlayerIndex = int("nextPlayerIndex") ?: return null,
        hasCompleteNumbering = boolean("hasCompleteNumbering", defaultValue = false),
        playerId = string("playerId"),
        total = int("total") ?: return null,
        pitPlayerIds = get("pitPlayerIds").asFirebaseStringList().toSet(),
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
