package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.RemoteUserInfo

object FirebaseRemoteUserInfoMapper {
    fun remoteUserInfo(from: Any?, key: String): RemoteUserInfo? =
        from.asFirebaseMap()?.toRemoteUserInfo(key)
}

private fun Map<*, *>.toRemoteUserInfo(key: String): RemoteUserInfo =
    RemoteUserInfo(
        id = key,
        name = string(NAME_FIELD) ?: "",
        platform = string(PLATFORM_FIELD) ?: "",
        gameCount = int(GAME_COUNT_FIELD) ?: 0,
        winCount = int(WIN_COUNT_FIELD) ?: 0,
    )

private const val NAME_FIELD = "name"
private const val PLATFORM_FIELD = "platform"
private const val GAME_COUNT_FIELD = "gameCount"
private const val WIN_COUNT_FIELD = "winCount"
