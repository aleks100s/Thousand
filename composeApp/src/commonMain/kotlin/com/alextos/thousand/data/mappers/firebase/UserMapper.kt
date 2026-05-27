package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object FirebaseUserMapper {
    fun dictionary(from: User): Map<String, Any> =
        from.toFirebaseMap()

    fun lobbyPlayerDictionary(from: User): Map<String, Any> =
        from.toFirebaseLobbyPlayerMap()

    fun user(from: Any?): User =
        from.asFirebaseMap().toFirebaseUser()

    fun lobbyPlayer(from: Any?): User =
        from.asFirebaseMap().toFirebaseLobbyPlayer()
}

internal fun User.toFirebaseMap(): Map<String, Any> =
    mapOf(
        "id" to id,
        "name" to name,
        "kind" to kind.name,
    )

internal fun User.toFirebaseLobbyPlayerMap(): Map<String, Any> =
    mapOf(
        "id" to id,
        "name" to name,
    )

@OptIn(ExperimentalUuidApi::class)
internal fun Map<*, *>?.toFirebaseUser(): User =
    User(
        id = string("id") ?: Uuid.random().toString(),
        name = string("name") ?: "Без имени",
        kind = this?.get("kind").toFirebaseUserKind(),
    )

@OptIn(ExperimentalUuidApi::class)
internal fun Map<*, *>?.toFirebaseLobbyPlayer(): User =
    User(
        id = string("id") ?: Uuid.random().toString(),
        name = string("name") ?: "Без имени",
        kind = UserKind.Remote,
    )

private fun Any?.toFirebaseUserKind(): UserKind {
    val kindName = this as? String
    if (kindName != null) {
        return UserKind.entries.firstOrNull { kind -> kind.name == kindName }
            ?: UserKind.LocalUser
    }

    val kindOrdinal = when (this) {
        is Int -> this
        is Long -> toInt()
        is Short -> toInt()
        is Byte -> toInt()
        is Double -> toInt()
        is Float -> toInt()
        is String -> toIntOrNull()
        else -> null
    }

    return kindOrdinal?.let { ordinal ->
        UserKind.entries.getOrElse(ordinal) { UserKind.LocalUser }
    } ?: UserKind.LocalUser
}
