package com.alextos.thousand.data.repository.mappers

import com.alextos.thousand.data.mappers.firebase.FirebaseUserMapper
import com.alextos.thousand.domain.models.User
import com.google.firebase.database.DataSnapshot

internal fun DataSnapshot.toUser(): User =
    FirebaseUserMapper.user(from = getValue())

internal fun User.toLobbyPlayerMap(): Map<String, Any> =
    FirebaseUserMapper.lobbyPlayerDictionary(from = this)

internal fun User.toDatabaseMap(): Map<String, Any> =
    FirebaseUserMapper.dictionary(from = this)

internal fun DataSnapshot.intValue(): Int? =
    getValue(Int::class.java)
        ?: getValue(Long::class.java)?.toInt()
        ?: getValue(String::class.java)?.toIntOrNull()

internal fun DataSnapshot.longValue(): Long? =
    getValue(Long::class.java)
        ?: getValue(Int::class.java)?.toLong()
        ?: getValue(String::class.java)?.toLongOrNull()
