package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.PlayerAvatar
import com.alextos.thousand.domain.models.PlayerColor
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object FirebaseUserMapper {
    fun appearanceDictionary(from: User): Map<String, Any> =
        mapOf("color" to from.color.name, "avatar" to from.avatar.name)

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
        "color" to color.name,
        "avatar" to avatar.name,
        "kind" to kind.name,
    )

internal fun User.toFirebaseLobbyPlayerMap(): Map<String, Any> =
    mapOf(
        "id" to id,
        "name" to name,
        "color" to color.name,
        "avatar" to avatar.name,
    )

@OptIn(ExperimentalUuidApi::class)
internal fun Map<*, *>?.toFirebaseUser(): User =
    User(
        id = string("id") ?: Uuid.random().toString(),
        name = string("name") ?: "Без имени",
        kind = this?.get("kind").toFirebaseUserKind(),
        color = appearanceColor(),
        avatar = appearanceAvatar(),
    )

@OptIn(ExperimentalUuidApi::class)
internal fun Map<*, *>?.toFirebaseLobbyPlayer(): User =
    User(
        id = string("id") ?: Uuid.random().toString(),
        name = string("name") ?: "Без имени",
        kind = UserKind.Remote,
        color = appearanceColor(),
        avatar = appearanceAvatar(),
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

private fun Map<*, *>?.appearanceColor() =
    PlayerColor.entries.firstOrNull { it.name == string("color") }
        ?: PlayerColor.defaultFor(string("id").orEmpty(), this?.get("kind").toFirebaseUserKind())

private fun Map<*, *>?.appearanceAvatar() =
    PlayerAvatar.entries.firstOrNull { it.name == string("avatar") }
        ?: PlayerAvatar.Neutral
