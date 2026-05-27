package com.alextos.thousand.data.repository.mappers

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.google.firebase.database.DataSnapshot

internal fun DataSnapshot.toUser(): User =
    User(
        id = child("id").getValue(String::class.java).orEmpty(),
        name = child("name").getValue(String::class.java) ?: "Без имени",
        kind = child("kind").toUserKind(),
    )

internal fun User.toLobbyPlayerMap(): Map<String, Any?> =
    mapOf(
        "id" to id,
        "name" to name,
    )

internal fun User.toDatabaseMap(): Map<String, Any?> =
    mapOf(
        "id" to id,
        "name" to name,
        "kind" to kind.name,
    )

internal fun DataSnapshot.intValue(): Int? =
    getValue(Int::class.java)
        ?: getValue(Long::class.java)?.toInt()
        ?: getValue(String::class.java)?.toIntOrNull()

internal fun DataSnapshot.longValue(): Long? =
    getValue(Long::class.java)
        ?: getValue(Int::class.java)?.toLong()
        ?: getValue(String::class.java)?.toLongOrNull()

private fun DataSnapshot.toUserKind(): UserKind {
    val kindName = getValue(String::class.java)
    if (kindName != null) {
        return UserKind.entries.firstOrNull { kind -> kind.name == kindName }
            ?: UserKind.LocalUser
    }

    val kindOrdinal = intValue()
    return kindOrdinal?.let { ordinal ->
        UserKind.entries.getOrElse(ordinal) { UserKind.LocalUser }
    } ?: UserKind.LocalUser
}
