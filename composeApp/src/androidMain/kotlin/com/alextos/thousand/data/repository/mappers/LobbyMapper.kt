package com.alextos.thousand.data.repository.mappers

import com.alextos.thousand.data.mappers.firebase.FirebaseGameSettingsMapper
import com.alextos.thousand.data.mappers.firebase.FirebaseLobbyMapper
import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.Lobby
import com.google.firebase.database.DataSnapshot

internal fun DataSnapshot.toLobby(): Lobby? {
    if (!exists()) return null

    return FirebaseLobbyMapper.lobby(from = value)
}

internal fun Lobby.toDatabaseMap(): Map<String, Any> =
    FirebaseLobbyMapper.dictionary(from = this)

internal fun GameSettings.toDatabaseMap(): Map<String, Any> =
    FirebaseGameSettingsMapper.dictionary(from = this)

internal fun DataSnapshot.toGameSettings(): GameSettings =
    FirebaseGameSettingsMapper.gameSettings(from = value)
